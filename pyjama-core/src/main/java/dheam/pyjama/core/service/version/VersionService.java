package dheam.pyjama.core.service.version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public final class VersionService {

    private static final String RESOURCE_NAME = "pyjama-build.properties";
    private static final String VERSION_KEY = "version";
    private static final String PROJECT_NAME_KEY = "project-name";
    private static final String DEFAULT_VERSION = "unknown";
    private static final String DEFAULT_PROJECT_NAME = "pyjama";
    private static final String DEFAULT_PLATFORM_ID = "unknown";

    private final String version;
    private final String projectName;

    public VersionService(Logger logger) {
        Properties properties = readProperties(logger);
        this.version = properties.getProperty(VERSION_KEY, DEFAULT_VERSION);
        this.projectName = properties.getProperty(PROJECT_NAME_KEY, DEFAULT_PROJECT_NAME);
    }

    public String getVersion() {
        return version;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getBuildName(String platformId) {
        String platform = platformId == null || platformId.isBlank() ? DEFAULT_PLATFORM_ID : platformId;
        return projectName + "-" + platform + "-" + version;
    }

    private Properties readProperties(Logger logger) {
        Properties properties = new Properties();
        try (InputStream stream = VersionService.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (stream == null) {
                logger.warning("Missing bundled " + RESOURCE_NAME + ", using default version metadata.");
                return properties;
            }
            properties.load(stream);
        } catch (IOException exception) {
            logger.warning("Failed to read " + RESOURCE_NAME + ": " + exception.getMessage());
        }
        return properties;
    }
}