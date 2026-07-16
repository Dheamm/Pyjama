package dheam.pyjama.core.service.locale;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public final class LocaleService {

    private static final String DEFAULT_LOCALE = "en";
    private static final String MISSING_KEY_FORMAT = "<red>Missing message: %s</red>";
    private static final String PREFIX_KEY = "prefix";
    private static final boolean MINIPLACEHOLDERS_AVAILABLE = detectMiniPlaceholders();

    private final Path dataFolder;
    private final Logger logger;
    private final Yaml yaml;
    private final MiniMessage miniMessage;

    private String locale;
    private Map<String, Object> messages;
    private String prefix;
    private final Map<String, String> globalPlaceholders = new ConcurrentHashMap<>();
    private final AtomicReference<TagResolver> globalResolver = new AtomicReference<>(TagResolver.empty());

    public LocaleService(Path dataFolder, Logger logger, String requestedLocale) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.yaml = createYaml();
        this.miniMessage = MiniMessage.miniMessage();
        load(requestedLocale);
    }

    public void reload(String requestedLocale) {
        load(requestedLocale);
    }

    public void setGlobalPlaceholder(String key, String value) {
        String normalized = value == null ? "" : value;
        if (normalized.equals(globalPlaceholders.get(key))) {
            return;
        }
        globalPlaceholders.put(key, normalized);
        rebuildGlobalResolver();
    }

    public void setGlobalPlaceholders(Map<String, String> values) {
        Map<String, String> normalized = new LinkedHashMap<>();
        values.forEach((key, value) -> normalized.put(key, value == null ? "" : value));
        if (normalized.equals(globalPlaceholders)) {
            return;
        }
        globalPlaceholders.clear();
        globalPlaceholders.putAll(normalized);
        rebuildGlobalResolver();
    }

    public String getLocale() {
        return locale;
    }

    public Component getMessage(String key) {
        return getMessage(null, key, TagResolver.empty());
    }

    public Component getMessage(String key, TagResolver... placeholders) {
        return getMessage(null, key, placeholders);
    }

    public Component getMessage(Audience audience, String key, TagResolver... placeholders) {
        return render(audience, getRaw(key), true, placeholders);
    }

    public Component getRawMessage(String key) {
        return getRawMessage(null, key, TagResolver.empty());
    }

    public Component getRawMessage(String key, TagResolver... placeholders) {
        return getRawMessage(null, key, placeholders);
    }

    public Component getRawMessage(Audience audience, String key, TagResolver... placeholders) {
        return render(audience, getRaw(key), false, placeholders);
    }

    public String getRaw(String key) {
        Object value = resolve(key);
        if (value instanceof String string) {
            return string;
        }
        logger.warning("Missing message key '" + key + "' for locale '" + locale + "'");
        return String.format(MISSING_KEY_FORMAT, key);
    }

    private Component render(Audience audience, String raw, boolean applyPrefix, TagResolver... placeholders) {
        String text = applyPrefix ? prefix + raw : raw;
        TagResolver resolver = TagResolver.resolver(TagResolver.resolver(placeholders), globalResolver.get());
        if (MINIPLACEHOLDERS_AVAILABLE) {
            TagResolver miniPlaceholdersResolver = audience != null
                    ? MiniPlaceholdersBridge.audiencePlaceholders()
                    : MiniPlaceholdersBridge.globalPlaceholders();
            resolver = TagResolver.resolver(resolver, miniPlaceholdersResolver);
        }
        return audience != null
                ? miniMessage.deserialize(text, audience, resolver)
                : miniMessage.deserialize(text, resolver);
    }

    private void rebuildGlobalResolver() {
        TagResolver.Builder builder = TagResolver.builder();
        globalPlaceholders.forEach((key, value) -> builder.resolver(Placeholder.unparsed(key, value)));
        globalResolver.set(builder.build());
    }

    private void load(String requestedLocale) {
        this.locale = normalizeLocale(requestedLocale);
        this.messages = new LinkedHashMap<>();

        Path file = fileFor(locale);
        ensureFileExists(file, locale);
        messages = readYaml(file, locale);

        if (!locale.equals(DEFAULT_LOCALE)) {
            Map<String, Object> englishDefaults = readDefaultResource(DEFAULT_LOCALE);
            mergeDefaults(messages, englishDefaults);
        }

        this.prefix = readPrefix();
    }

    private String readPrefix() {
        Object value = resolve(PREFIX_KEY);
        if (value instanceof String string) {
            return string;
        }
        if (value != null) {
            logger.warning("Config value at '" + PREFIX_KEY + "' has an invalid type, using empty prefix");
        }
        return "";
    }

    private String normalizeLocale(String requestedLocale) {
        if (requestedLocale == null || requestedLocale.isBlank()) {
            return DEFAULT_LOCALE;
        }
        return requestedLocale;
    }

    private Path fileFor(String targetLocale) {
        return dataFolder.resolve(fileNameFor(targetLocale));
    }

    private String fileNameFor(String targetLocale) {
        return "messages_" + targetLocale + ".yml";
    }

    private Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }

    private void ensureFileExists(Path file, String targetLocale) {
        if (Files.exists(file)) {
            return;
        }
        try (InputStream stream = openResourceStream(targetLocale)) {
            if (stream == null) {
                return;
            }
            Path parent = file.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.copy(stream, file);
        } catch (IOException exception) {
            logger.warning("Failed to create default " + fileNameFor(targetLocale) + ": " + exception.getMessage());
        }
    }

    private Map<String, Object> readYaml(Path source, String targetLocale) {
        if (!Files.exists(source)) {
            logger.warning("Locale file '" + fileNameFor(targetLocale) + "' not found, using English defaults for missing keys.");
            return new LinkedHashMap<>();
        }
        try (InputStream stream = Files.newInputStream(source)) {
            Map<String, Object> loaded = yaml.load(stream);
            return loaded != null ? loaded : new LinkedHashMap<>();
        } catch (Exception exception) {
            logger.warning("Failed to read " + fileNameFor(targetLocale) + ", falling back to defaults: " + exception.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> readDefaultResource(String targetLocale) {
        try (InputStream stream = openResourceStream(targetLocale)) {
            if (stream == null) {
                return new LinkedHashMap<>();
            }
            Map<String, Object> loaded = yaml.load(stream);
            return loaded != null ? loaded : new LinkedHashMap<>();
        } catch (IOException exception) {
            logger.warning("Failed to read bundled " + fileNameFor(targetLocale) + ": " + exception.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private InputStream openResourceStream(String targetLocale) {
        return LocaleService.class.getClassLoader().getResourceAsStream(fileNameFor(targetLocale));
    }

    @SuppressWarnings("unchecked")
    private boolean mergeDefaults(Map<String, Object> target, Map<String, Object> defaults) {
        boolean changed = false;
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            String key = entry.getKey();
            Object defaultValue = entry.getValue();
            if (!target.containsKey(key)) {
                target.put(key, defaultValue);
                changed = true;
            } else if (defaultValue instanceof Map<?, ?> && target.get(key) instanceof Map<?, ?>) {
                changed |= mergeDefaults((Map<String, Object>) target.get(key), (Map<String, Object>) defaultValue);
            }
        }
        return changed;
    }

    private void save(Path file) {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            yaml.dump(messages, writer);
        } catch (IOException exception) {
            logger.warning("Failed to save " + file.getFileName() + ": " + exception.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Object resolve(String path) {
        Object current = messages;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?>)) {
                return null;
            }
            current = ((Map<String, Object>) current).get(part);
        }
        return current;
    }

    private static boolean detectMiniPlaceholders() {
        try {
            Class.forName("io.github.miniplaceholders.api.MiniPlaceholders");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}