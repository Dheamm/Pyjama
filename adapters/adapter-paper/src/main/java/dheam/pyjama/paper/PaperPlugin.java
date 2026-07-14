package dheam.pyjama.paper;

import dheam.pyjama.core.PyjamaCore;
import dheam.pyjama.core.service.permission.Permissions;
import dheam.pyjama.paper.command.CommandRegistrar;
import dheam.pyjama.paper.command.PyjamaCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperPlugin extends JavaPlugin {

    private PyjamaCore core;
    private PyjamaCommand pyjamaCommand;

    @Override
    public void onEnable() {
        PaperPlatformBridge platformBridge = new PaperPlatformBridge(this);
        core = new PyjamaCore(getDataFolder().toPath(), platformBridge, getLogger());
        core.enable();
        registerCommand();
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

    private void registerCommand() {
        pyjamaCommand = new PyjamaCommand(
                core.getConfigService().getCommandName(),
                core.getConfigService().getCommandAliases(),
                core,
                this::rebindCommand
        );
        CommandRegistrar.register(this, pyjamaCommand);
    }

    private void rebindCommand() {
        CommandRegistrar.unregister(this, pyjamaCommand);
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