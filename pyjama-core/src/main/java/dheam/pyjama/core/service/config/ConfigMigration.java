package dheam.pyjama.core.service.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

final class ConfigMigration {

    static final int CURRENT_VERSION = 1;

    private ConfigMigration() {
    }

    static boolean migrate(Map<String, Object> config, Logger logger) {
        int storedVersion = readVersion(config);
        if (storedVersion >= CURRENT_VERSION) {
            return false;
        }

        logger.warning("Migrating config from version " + storedVersion + " to " + CURRENT_VERSION);
        int version = storedVersion;
        while (version < CURRENT_VERSION) {
            version = applyStep(config, version, logger);
        }
        config.put("config-version", CURRENT_VERSION);
        return true;
    }

    private static int applyStep(Map<String, Object> config, int fromVersion, Logger logger) {
        return switch (fromVersion) {
            default -> {
                logger.warning("No migration step defined from version " + fromVersion + ", forcing to current.");
                yield CURRENT_VERSION;
            }
        };
    }

    private static int readVersion(Map<String, Object> config) {
        Object value = config.get("config-version");
        if (value instanceof Integer integer) {
            return integer;
        }
        return CURRENT_VERSION;
    }

    static Map<String, Object> empty() {
        return new LinkedHashMap<>();
    }
}