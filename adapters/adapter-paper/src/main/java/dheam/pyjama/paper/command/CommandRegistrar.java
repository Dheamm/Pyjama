package dheam.pyjama.paper.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

public final class CommandRegistrar {

    private CommandRegistrar() {
    }

    public static void register(Plugin plugin, Command command) {
        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command);
    }

    public static void unregister(Plugin plugin, Command command) {
        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.getKnownCommands().values().removeIf(registered -> registered == command);
    }
}