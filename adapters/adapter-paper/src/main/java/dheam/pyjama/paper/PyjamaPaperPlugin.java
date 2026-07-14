package dheam.pyjama.paper;

import dheam.pyjama.core.PyjamaCore;
import org.bukkit.plugin.java.JavaPlugin;

public final class PyjamaPaperPlugin extends JavaPlugin {

    private PyjamaCore core;

    @Override
    public void onEnable() {
        core = new PyjamaCore(getDataFolder(), getLogger());
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