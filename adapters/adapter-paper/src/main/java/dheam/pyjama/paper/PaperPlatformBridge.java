package dheam.pyjama.paper;

import dheam.pyjama.api.platform.PlatformBridge;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PaperPlatformBridge implements PlatformBridge {

    private static final String RESOURCE_NAME = "pyjama-platform.properties";
    private static final String PLATFORM_ID_KEY = "platform-id";
    private static final String DEFAULT_PLATFORM_ID = "paper";

    private final Plugin plugin;
    private final String platformId;

    public PaperPlatformBridge(Plugin plugin) {
        this.plugin = plugin;
        this.platformId = readPlatformId();
    }

    @Override
    public String platformId() {
        return platformId;
    }

    private String readPlatformId() {
        Properties properties = new Properties();
        try (InputStream stream = PaperPlatformBridge.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (stream == null) {
                plugin.getLogger().warning("Missing bundled " + RESOURCE_NAME + ", using default platform id.");
                return DEFAULT_PLATFORM_ID;
            }
            properties.load(stream);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to read " + RESOURCE_NAME + ": " + exception.getMessage());
        }
        return properties.getProperty(PLATFORM_ID_KEY, DEFAULT_PLATFORM_ID);
    }
}