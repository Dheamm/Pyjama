package dheam.pyjama.core.service.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class LocaleService {

    private static final String DEFAULT_LOCALE = "en";
    private static final List<String> SUPPORTED_LOCALES = List.of("en", "es");
    private static final String MISSING_KEY_FORMAT = "<red>Missing message: %s</red>";

    private final File dataFolder;
    private final Logger logger;
    private final Yaml yaml;
    private final MiniMessage miniMessage;

    private String locale;
    private Map<String, Object> messages;

    public LocaleService(File dataFolder, Logger logger, String requestedLocale) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.yaml = createYaml();
        this.miniMessage = MiniMessage.miniMessage();
        load(requestedLocale);
    }

    public void reload(String requestedLocale) {
        load(requestedLocale);
    }

    public String getLocale() {
        return locale;
    }

    public Component getMessage(String key) {
        return getMessage(key, TagResolver.empty());
    }

    public Component getMessage(String key, TagResolver... placeholders) {
        String raw = getRaw(key);
        return miniMessage.deserialize(raw, placeholders);
    }

    public String getRaw(String key) {
        Object value = resolve(key);
        if (value instanceof String) {
            return (String) value;
        }
        logger.warning("Missing message key '" + key + "' for locale '" + locale + "'");
        return String.format(MISSING_KEY_FORMAT, key);
    }

    private void load(String requestedLocale) {
        this.locale = normalizeLocale(requestedLocale);
        this.messages = new LinkedHashMap<>();

        File file = fileFor(locale);
        ensureFileExists(file, locale);
        messages = readYaml(file, locale);

        Map<String, Object> defaults = readDefaultResource(locale);
        if (mergeDefaults(messages, defaults)) {
            save(file);
        }

        if (!locale.equals(DEFAULT_LOCALE)) {
            Map<String, Object> englishDefaults = readDefaultResource(DEFAULT_LOCALE);
            mergeDefaults(messages, englishDefaults);
        }
    }

    private String normalizeLocale(String requestedLocale) {
        if (requestedLocale == null || !SUPPORTED_LOCALES.contains(requestedLocale)) {
            if (requestedLocale != null) {
                logger.warning("Unsupported locale '" + requestedLocale + "', falling back to '" + DEFAULT_LOCALE + "'");
            }
            return DEFAULT_LOCALE;
        }
        return requestedLocale;
    }

    private File fileFor(String targetLocale) {
        return new File(dataFolder, fileNameFor(targetLocale));
    }

    private String fileNameFor(String targetLocale) {
        return "messages_" + targetLocale + ".yml";
    }

    private Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }

    private void ensureFileExists(File file, String targetLocale) {
        if (file.exists()) {
            return;
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (InputStream stream = openResourceStream(targetLocale)) {
            if (stream == null) {
                return;
            }
            Files.copy(stream, file.toPath());
        } catch (IOException exception) {
            logger.warning("Failed to create default " + fileNameFor(targetLocale) + ": " + exception.getMessage());
        }
    }

    private Map<String, Object> readYaml(File source, String targetLocale) {
        try (InputStream stream = new FileInputStream(source)) {
            Map<String, Object> loaded = yaml.load(stream);
            return loaded != null ? loaded : new LinkedHashMap<>();
        } catch (Exception exception) {
            logger.warning("Failed to read " + fileNameFor(targetLocale) + ", falling back to defaults: " + exception.getMessage());
            return readDefaultResource(targetLocale);
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

    private void save(File file) {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            yaml.dump(messages, new java.io.OutputStreamWriter(stream));
        } catch (IOException exception) {
            logger.warning("Failed to save " + file.getName() + ": " + exception.getMessage());
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
}