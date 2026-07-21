package dheam.pyjama.api.command;

import net.kyori.adventure.text.Component;

public interface CommandContext {

    void sendMessage(Component message);

    boolean hasPermission(String permission);
}