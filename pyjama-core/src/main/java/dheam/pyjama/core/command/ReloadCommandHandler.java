package dheam.pyjama.core.command;

import dheam.pyjama.api.command.CommandContext;
import dheam.pyjama.api.command.ReloadCommand;
import dheam.pyjama.core.PyjamaCore;
import dheam.pyjama.core.service.locale.LocaleService;
import dheam.pyjama.core.service.permission.Permissions;

public final class ReloadCommandHandler implements ReloadCommand {

    private final PyjamaCore core;
    private final LocaleService localeService;

    public ReloadCommandHandler(PyjamaCore core, LocaleService localeService) {
        this.core = core;
        this.localeService = localeService;
    }

    @Override
    public void reload(CommandContext context) {
        if (!context.hasPermission(Permissions.COMMAND_RELOAD)) {
            context.sendMessage(localeService.getMessage("command.no-permission"));
            return;
        }
        core.reload();
        context.sendMessage(localeService.getMessage("command.reload-success"));
    }
}