package dheam.pyjama.core.service.config;

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

public final class ConfigService {

    private static final String FILE_NAME = "config.yml";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final String DEFAULT_COMMAND_NAME = "pyjama";
    private static final List<String> DEFAULT_COMMAND_ALIASES = List.of("pj");

    private final File file;
    private final Logger logger;
    private final Yaml yaml;
    private Map<String, Object> config;

    public ConfigService(File dataFolder, Logger logger) {
        this.file = new File(dataFolder, FILE_NAME);
        this.logger = logger;
        this.yaml = createYaml();
        load();
    }

    public void reload() {
        load();
    }

    public int getConfigVersion() {
        return getIntSafe("config-version", CURRENT_CONFIG_VERSION);
    }

    public String getCommandName() {
        return getStringSafe("command.name", DEFAULT_COMMAND_NAME);
    }

    public List<String> getCommandAliases() {
        return getStringListSafe("command.aliases", DEFAULT_COMMAND_ALIASES);
    }

    private Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }

    private void load() {
        ensureFileExists();
        config = readYaml(file);

        Map<String, Object> defaults = readDefaultResource();
        if (mergeDefaults(config, defaults)) {
            save();
        }
    }

    private void ensureFileExists() {
        if (file.exists()) {
            return;
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (InputStream stream = openDefaultResourceStream()) {
            if (stream == null) {
                return;
            }
            Files.copy(stream, file.toPath());
        } catch (IOException exception) {
            logger.warning("Failed to create default " + FILE_NAME + ": " + exception.getMessage());
        }
    }

    private Map<String, Object> readYaml(File source) {
        try (InputStream stream = new FileInputStream(source)) {
            Map<String, Object> loaded = yaml.load(stream);
            return loaded != null ? loaded : new LinkedHashMap<>();
        } catch (Exception exception) {
            logger.warning("Failed to read " + FILE_NAME + ", falling back to defaults: " + exception.getMessage());
            return readDefaultResource();
        }
    }

    private Map<String, Object> readDefaultResource() {
        try (InputStream stream = openDefaultResourceStream()) {
            if (stream == null) {
                return new LinkedHashMap<>();
            }
            Map<String, Object> loaded = yaml.load(stream);
            return loaded != null ? loaded : new LinkedHashMap<>();
        } catch (IOException exception) {
            logger.warning("Failed to read bundled " + FILE_NAME + ": " + exception.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private InputStream openDefaultResourceStream() {
        return ConfigService.class.getClassLoader().getResourceAsStream(FILE_NAME);
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

    private void save() {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            yaml.dump(config, new java.io.OutputStreamWriter(stream));
        } catch (IOException exception) {
            logger.warning("Failed to save " + FILE_NAME + ": " + exception.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Object resolve(String path) {
        Object current = config;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?>)) {
                return null;
            }
            current = ((Map<String, Object>) current).get(part);
        }
        return current;
    }

    private int getIntSafe(String path, int fallback) {
        Object value = resolve(path);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        warnIfInvalidType(path, value, fallback);
        return fallback;
    }

    private String getStringSafe(String path, String fallback) {
        Object value = resolve(path);
        if (value instanceof String) {
            return (String) value;
        }
        warnIfInvalidType(path, value, fallback);
        return fallback;
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringListSafe(String path, List<String> fallback) {
        Object value = resolve(path);
        if (value instanceof List<?>) {
            return (List<String>) value;
        }
        warnIfInvalidType(path, value, fallback);
        return fallback;
    }

    private void warnIfInvalidType(String path, Object value, Object fallback) {
        if (value != null) {
            logger.warning("Config value at '" + path + "' has an invalid type, using default '" + fallback + "'");
        }
    }
}