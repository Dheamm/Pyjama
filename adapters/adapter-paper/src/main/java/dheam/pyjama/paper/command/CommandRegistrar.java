package dheam.pyjama.paper.command;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

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
        if (!removeFromKnownCommands(commandMap, command)) {
            plugin.getLogger().warning(
                    "Could not purge old name/aliases for command '" + command.getName()
                            + "' from the command map; they may keep working until the server restarts."
            );
        }
    }

    private static CommandMap resolveCommandMap(Server server) {
        Field field = findField(server.getClass(), "commandMap");
        if (field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return (CommandMap) field.get(server);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean removeFromKnownCommands(CommandMap commandMap, Command command) {
        Field field = findField(commandMap.getClass(), "knownCommands");
        if (field == null) {
            return false;
        }
        try {
            field.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>) field.get(commandMap);
            knownCommands.values().removeIf(registered -> registered == command);
            return true;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }
}