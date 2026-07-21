package dheam.pyjama.paper;

import dheam.pyjama.api.command.CommandTreeRebinder;
import dheam.pyjama.core.PyjamaCore;
import dheam.pyjama.paper.command.CommandRegistrar;
import dheam.pyjama.paper.command.PaperCommandTreeRebinder;
import dheam.pyjama.paper.command.RootCommandNode;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class PaperPlugin extends JavaPlugin {

    private PyjamaCore core;
    private CommandTreeRebinder commandTreeRebinder;
    private String registeredCommandName;
    private List<String> registeredAliases;

    @Override
    public void onEnable() {
        PaperPlatformBridge platformBridge = new PaperPlatformBridge(this);
        core = new PyjamaCore(getDataFolder().toPath(), platformBridge, getLogger());
        core.enable();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commandsRegistrar = event.registrar();
            registeredCommandName = core.getConfigService().getCommandName();
            registeredAliases = core.getConfigService().getCommandAliases();

            CommandRegistrar.register(
                    commandsRegistrar,
                    RootCommandNode.build(registeredCommandName, core, this::rebindCommand),
                    "Pyjama main command.",
                    registeredAliases
            );

            commandTreeRebinder = new PaperCommandTreeRebinder(
                    this,
                    commandsRegistrar.getDispatcher(),
                    name -> RootCommandNode.build(name, core, this::rebindCommand)
            );
        });
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

    private void rebindCommand() {
        if (commandTreeRebinder == null) {
            return;
        }

        String newName = core.getConfigService().getCommandName();
        List<String> newAliases = core.getConfigService().getCommandAliases();

        commandTreeRebinder.rebind(registeredCommandName, registeredAliases, newName, newAliases);

        registeredCommandName = newName;
        registeredAliases = newAliases;
    }
}