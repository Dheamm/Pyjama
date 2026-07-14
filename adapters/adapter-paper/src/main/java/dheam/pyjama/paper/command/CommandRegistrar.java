package dheam.pyjama.paper.command;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Locale;

public final class CommandRegistrar {

    private CommandRegistrar() {
    }

    public static void register(Plugin plugin, Command command) {
        CommandMap commandMap = resolveCommandMap(plugin.getServer());
        if (commandMap == null) {
            plugin.getLogger().warning("Could not register command '" + command.getName() + "': command map unavailable.");
            return;
        }
        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command);
    }

    public static void unregister(Plugin plugin, Command command) {
        CommandMap commandMap = resolveCommandMap(plugin.getServer());
        if (commandMap == null) {
            return;
        }
        command.unregister(commandMap);
    }

    private static CommandMap resolveCommandMap(Server server) {
        try {
            Field field = server.getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(server);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}