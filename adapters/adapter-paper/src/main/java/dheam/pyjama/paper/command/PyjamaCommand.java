package dheam.pyjama.paper.command;

import dheam.pyjama.core.PyjamaCore;
import dheam.pyjama.core.service.permission.Permissions;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PyjamaCommand extends Command {

    private static final List<String> SUBCOMMANDS = List.of("hello", "reload");

    private final PyjamaCore core;

    public PyjamaCommand(String name, List<String> aliases, PyjamaCore core) {
        super(name);
        setAliases(aliases);
        this.core = core;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, commandLabel);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "hello" -> handleHello(sender);
            case "reload" -> handleReload(sender);
            default -> sendUsage(sender, commandLabel);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }

        String partial = args[0].toLowerCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();
        for (String subcommand : SUBCOMMANDS) {
            if (subcommand.startsWith(partial) && hasPermissionFor(sender, subcommand)) {
                completions.add(subcommand);
            }
        }
        return completions;
    }

    private void handleHello(CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_HELLO)) {
            sendNoPermission(sender);
            return;
        }
        sender.sendMessage(core.getLocaleService().getMessage("command.hello"));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_RELOAD)) {
            sendNoPermission(sender);
            return;
        }
        core.reload();
        sender.sendMessage(core.getLocaleService().getMessage("command.reload-success"));
    }

    private void sendUsage(CommandSender sender, String commandLabel) {
        sender.sendMessage(core.getLocaleService().getMessage(
                "command.usage",
                Placeholder.unparsed("label", commandLabel)
        ));
    }

    private void sendNoPermission(CommandSender sender) {
        sender.sendMessage(core.getLocaleService().getMessage("command.no-permission"));
    }

    private boolean hasPermissionFor(CommandSender sender, String subcommand) {
        return switch (subcommand) {
            case "hello" -> sender.hasPermission(Permissions.COMMAND_HELLO);
            case "reload" -> sender.hasPermission(Permissions.COMMAND_RELOAD);
            default -> false;
        };
    }
}