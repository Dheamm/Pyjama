package dheam.pyjama.core.command;

import dheam.pyjama.api.command.CommandContext;
import dheam.pyjama.api.command.RootCommand;
import dheam.pyjama.core.service.locale.LocaleService;
import dheam.pyjama.core.service.permission.Permissions;

public final class RootCommandHandler implements RootCommand {

    private final LocaleService localeService;

    public RootCommandHandler(LocaleService localeService) {
        this.localeService = localeService;
    }

    @Override
    public void info(CommandContext context) {
        if (!context.hasPermission(Permissions.COMMAND_INFO)) {
            context.sendMessage(localeService.getMessage("command.no-permission"));
            return;
        }
        context.sendMessage(localeService.getMessage("command.info"));
    }
}