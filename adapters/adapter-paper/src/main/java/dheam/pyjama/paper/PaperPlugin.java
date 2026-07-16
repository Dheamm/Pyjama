package dheam.pyjama.paper;

import dheam.pyjama.core.PyjamaCore;
import dheam.pyjama.paper.command.CommandRegistrar;
import dheam.pyjama.paper.command.RootCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperPlugin extends JavaPlugin {

    private PyjamaCore core;
    private RootCommand rootCommand;

    @Override
    public void onEnable() {
        PaperPlatformBridge platformBridge = new PaperPlatformBridge(this);
        core = new PyjamaCore(getDataFolder().toPath(), platformBridge, getLogger());
        core.enable();
        registerCommand();
    }

    @Override
    public void onDisable() {
        if (rootCommand != null) {
            CommandRegistrar.unregister(this, rootCommand);
        }
        if (core != null) {
            core.disable();
        }
    }

    public PyjamaCore getCore() {
        return core;
    }

    private void registerCommand() {
        rootCommand = new RootCommand(
                core.getConfigService().getCommandName(),
                core.getConfigService().getCommandAliases(),
                core,
                this::rebindCommand
        );
        CommandRegistrar.register(this, rootCommand);
    }

    private void rebindCommand() {
        CommandRegistrar.unregister(this, rootCommand);
        registerCommand();
        getServer().getOnlinePlayers().stream()
                .filter(this::hasAnyPyjamaPermission)
                .forEach(Player::updateCommands);
    }

    private boolean hasAnyPyjamaPermission(Player player) {
        return player.isOp()
                || player.getEffectivePermissions().stream()
                .anyMatch(info -> info.getPermission().startsWith("pyjama.") && info.getValue());
    }
}