package dheam.pyjama.paper.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dheam.pyjama.core.PyjamaCore;
import dheam.pyjama.core.service.permission.Permissions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public final class RootCommandNode {

    private RootCommandNode() {
    }

    public static LiteralCommandNode<CommandSourceStack> build(String name, PyjamaCore core, Runnable postReloadHook) {
        return Commands.literal(name)
                .requires(source -> source.getSender().hasPermission(Permissions.COMMAND_INFO)
                        || source.getSender().hasPermission(Permissions.COMMAND_RELOAD))
                .executes(context -> {
                    handleInfo(context.getSource(), core);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission(Permissions.COMMAND_RELOAD))
                        .executes(context -> {
                            handleReload(context.getSource(), core, postReloadHook);
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        }))
                .build();
    }

    private static void handleInfo(CommandSourceStack source, PyjamaCore core) {
        if (!source.getSender().hasPermission(Permissions.COMMAND_INFO)) {
            sendNoPermission(source, core);
            return;
        }
        source.getSender().sendMessage(core.getLocaleService().getMessage(source.getSender(), "command.info"));
    }

    private static void handleReload(CommandSourceStack source, PyjamaCore core, Runnable postReloadHook) {
        core.reload();
        source.getSender().sendMessage(core.getLocaleService().getMessage(source.getSender(), "command.reload-success"));
        if (postReloadHook != null) {
            postReloadHook.run();
        }
    }

    private static void sendNoPermission(CommandSourceStack source, PyjamaCore core) {
        source.getSender().sendMessage(core.getLocaleService().getMessage(source.getSender(), "command.no-permission"));
    }
}