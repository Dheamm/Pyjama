package dheam.pyjama.core;

import org.bukkit.plugin.java.JavaPlugin;

public final class PyjamaCore extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Pyjama enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Pyjama disabled.");
    }
}
