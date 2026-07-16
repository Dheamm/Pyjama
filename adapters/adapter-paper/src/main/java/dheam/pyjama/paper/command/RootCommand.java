package dheam.pyjama.paper.command;

import dheam.pyjama.core.PyjamaCore;
import dheam.pyjama.core.service.permission.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RootCommand extends Command {

    private static final List<String> SUBCOMMANDS = List.of("reload");

    private final PyjamaCore core;
    private final Runnable postReloadHook;

    public RootCommand(String name, List<String> aliases, PyjamaCore core, Runnable postReloadHook) {
        super(name);
        setAliases(aliases);
        this.core = core;
        this.postReloadHook = postReloadHook;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 0) {
            handleInfo(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> handleReload(sender);
            default -> handleInfo(sender);
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

    private void handleInfo(CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_INFO)) {
            sendNoPermission(sender);
            return;
        }
        sender.sendMessage(core.getLocaleService().getMessage(sender, "command.info"));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_RELOAD)) {
            sendNoPermission(sender);
            return;
        }
        core.reload();
        sender.sendMessage(core.getLocaleService().getMessage(sender, "command.reload-success"));
        if (postReloadHook != null) {
            postReloadHook.run();
        }
    }

    private void sendNoPermission(CommandSender sender) {
        sender.sendMessage(core.getLocaleService().getMessage(sender, "command.no-permission"));
    }

    private boolean hasPermissionFor(CommandSender sender, String subcommand) {
        return switch (subcommand) {
            case "reload" -> sender.hasPermission(Permissions.COMMAND_RELOAD);
            default -> false;
        };
    }
}