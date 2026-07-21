package dheam.pyjama.paper.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dheam.pyjama.core.command.RootCommandHandler;
import dheam.pyjama.core.service.permission.Permissions;
import dheam.pyjama.paper.command.support.PaperCommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public final class PaperRootCommandNode {

    private PaperRootCommandNode() {
    }

    public static LiteralCommandNode<CommandSourceStack> build(String name, RootCommandHandler handler) {
        return Commands.literal(name)
                .requires(source -> source.getSender().hasPermission(Permissions.COMMAND_INFO)
                        || source.getSender().hasPermission(Permissions.COMMAND_RELOAD))
                .executes(context -> {
                    handler.info(new PaperCommandContext(context.getSource()));
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                })
                .build();
    }
}