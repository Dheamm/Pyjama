package dheam.pyjama.paper;

import dheam.pyjama.core.PyjamaCore;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperPlugin extends JavaPlugin {

    private PyjamaCore core;

    @Override
    public void onEnable() {
        PaperPlatformBridge platformBridge = new PaperPlatformBridge(this);
        core = new PyjamaCore(getDataFolder().toPath(), platformBridge, getLogger());
        core.enable();
    }

    @Override
    public void onDisable() {
        if (core != null) {
            core.disable();
        }
    }

    public PyjamaCore getCore() {
        return core;
    }
}