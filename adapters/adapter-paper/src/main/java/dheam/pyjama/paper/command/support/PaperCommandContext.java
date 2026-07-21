package dheam.pyjama.paper.command.support;

import dheam.pyjama.api.command.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;

public final class PaperCommandContext implements CommandContext {

    private final CommandSourceStack source;

    public PaperCommandContext(CommandSourceStack source) {
        this.source = source;
    }

    @Override
    public void sendMessage(Component message) {
        source.getSender().sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.getSender().hasPermission(permission);
    }
}