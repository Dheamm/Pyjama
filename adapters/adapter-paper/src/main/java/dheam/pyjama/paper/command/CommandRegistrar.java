package dheam.pyjama.paper.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.List;

public final class CommandRegistrar {

    private CommandRegistrar() {
    }

    public static void register(Commands commands, LiteralCommandNode<CommandSourceStack> node,
                                String description, List<String> aliases) {
        commands.register(node, description, aliases);
    }
}