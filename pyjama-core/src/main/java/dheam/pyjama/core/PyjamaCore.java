package dheam.pyjama.core;

import dheam.pyjama.core.service.config.ConfigService;
import org.bukkit.plugin.java.JavaPlugin;

public final class PyjamaCore extends JavaPlugin {

    private ConfigService configService;

    @Override
    public void onEnable() {
        configService = new ConfigService(this);
        getLogger().info("Pyjama enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Pyjama disabled.");
    }

    public ConfigService getConfigService() {
        return configService;
    }
}