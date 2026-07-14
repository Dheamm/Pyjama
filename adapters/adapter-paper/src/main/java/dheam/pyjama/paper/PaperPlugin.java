package dheam.pyjama.paper;

import dheam.pyjama.core.PyjamaCore;
import dheam.pyjama.paper.command.CommandRegistrar;
import dheam.pyjama.paper.command.PyjamaCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperPlugin extends JavaPlugin {

    private PyjamaCore core;
    private PyjamaCommand pyjamaCommand;

    @Override
    public void onEnable() {
        PaperPlatformBridge platformBridge = new PaperPlatformBridge(this);
        core = new PyjamaCore(getDataFolder().toPath(), platformBridge, getLogger());
        core.enable();

        pyjamaCommand = new PyjamaCommand(
                core.getConfigService().getCommandName(),
                core.getConfigService().getCommandAliases(),
                core
        );
        CommandRegistrar.register(this, pyjamaCommand);
    }

    @Override
    public void onDisable() {
        if (pyjamaCommand != null) {
            CommandRegistrar.unregister(this, pyjamaCommand);
        }
        if (core != null) {
            core.disable();
        }
    }

    public PyjamaCore getCore() {
        return core;
    }
}