package dheam.pyjama.paper.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dheam.pyjama.core.command.CommandHandlers;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public final class PaperCommandTree {

    private PaperCommandTree() {
    }

    public static LiteralCommandNode<CommandSourceStack> build(String name, CommandHandlers handlers,
                                                               Runnable postReloadHook) {
        LiteralCommandNode<CommandSourceStack> root = PaperRootCommandNode.build(name, handlers.root());
        root.addChild(PaperReloadCommandNode.build(handlers.reload(), postReloadHook));
        return root;
    }
}