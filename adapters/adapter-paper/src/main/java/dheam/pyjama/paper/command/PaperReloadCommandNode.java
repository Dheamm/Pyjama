package dheam.pyjama.paper.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dheam.pyjama.core.command.ReloadCommandHandler;
import dheam.pyjama.core.service.permission.Permissions;
import dheam.pyjama.paper.command.support.PaperCommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public final class PaperReloadCommandNode {

    private PaperReloadCommandNode() {
    }

    public static LiteralCommandNode<CommandSourceStack> build(ReloadCommandHandler handler, Runnable postReloadHook) {
        return Commands.literal("reload")
                .requires(source -> source.getSender().hasPermission(Permissions.COMMAND_RELOAD))
                .executes(context -> {
                    handler.reload(new PaperCommandContext(context.getSource()));
                    if (postReloadHook != null) {
                        postReloadHook.run();
                    }
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                })
                .build();
    }
}