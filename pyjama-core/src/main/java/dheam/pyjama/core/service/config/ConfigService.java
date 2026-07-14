package dheam.pyjama.core.service.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

public final class ConfigService {

    private static final String FILE_NAME = "config.yml";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final String DEFAULT_COMMAND_NAME = "pyjama";
    private static final List<String> DEFAULT_COMMAND_ALIASES = List.of("pj");

    private final JavaPlugin plugin;
    private final Logger logger;
    private final File file;
    private YamlConfiguration config;

    public ConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
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

    private void load() {
        ensureFileExists();
        config = loadYaml(file);

        YamlConfiguration defaults = loadDefaultResource();
        if (mergeDefaults(config, defaults)) {
            save();
        }
    }

    private void ensureFileExists() {
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
    }

    private YamlConfiguration loadYaml(File source) {
        YamlConfiguration loaded = new YamlConfiguration();
        try {
            loaded.load(source);
            return loaded;
        } catch (IOException | InvalidConfigurationException exception) {
            logger.warning("Failed to read " + FILE_NAME + ", falling back to defaults: " + exception.getMessage());
            return loadDefaultResource();
        }
    }

    private YamlConfiguration loadDefaultResource() {
        try (InputStream stream = plugin.getResource(FILE_NAME)) {
            if (stream == null) {
                return new YamlConfiguration();
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            logger.warning("Failed to read bundled " + FILE_NAME + ": " + exception.getMessage());
            return new YamlConfiguration();
        }
    }

    private boolean mergeDefaults(YamlConfiguration target, YamlConfiguration defaults) {
        boolean changed = false;
        for (String key : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(key)) {
                continue;
            }
            if (!target.isSet(key)) {
                target.set(key, defaults.get(key));
                changed = true;
            }
        }
        return changed;
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            logger.warning("Failed to save " + FILE_NAME + ": " + exception.getMessage());
        }
    }

    private int getIntSafe(String path, int fallback) {
        if (!config.isInt(path)) {
            warnIfInvalidType(path, fallback);
            return fallback;
        }
        return config.getInt(path);
    }

    private String getStringSafe(String path, String fallback) {
        if (!config.isString(path)) {
            warnIfInvalidType(path, fallback);
            return fallback;
        }
        return config.getString(path, fallback);
    }

    private List<String> getStringListSafe(String path, List<String> fallback) {
        if (!config.isList(path)) {
            warnIfInvalidType(path, fallback);
            return fallback;
        }
        return config.getStringList(path);
    }

    private void warnIfInvalidType(String path, Object fallback) {
        if (config.isSet(path)) {
            logger.warning("Config value at '" + path + "' has an invalid type, using default '" + fallback + "'");
        }
    }
}