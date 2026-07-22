package dheam.pyjama.core.command;

import dheam.pyjama.api.command.CommandContext;
import dheam.pyjama.api.command.ReloadCommand;
import dheam.pyjama.core.PyjamaCore;
import dheam.pyjama.core.service.locale.LocaleService;
import dheam.pyjama.core.service.permission.Permissions;
import dheam.pyjama.core.service.placeholder.PlaceholderService;
import dheam.pyjama.core.service.placeholder.Placeholders;

public final class ReloadCommandHandler implements ReloadCommand {

    private static final String NAMESPACE = "reload";

    private final PyjamaCore core;
    private final LocaleService localeService;
    private final PlaceholderService placeholderService;

    public ReloadCommandHandler(PyjamaCore core, LocaleService localeService, PlaceholderService placeholderService) {
        this.core = core;
        this.localeService = localeService;
        this.placeholderService = placeholderService;
        placeholderService.registerLocal(NAMESPACE, Placeholders.Reload.TARGET, () -> "all");
    }

    @Override
    public void reload(CommandContext context) {
        if (!context.hasPermission(Permissions.COMMAND_RELOAD)) {
            context.sendMessage(localeService.getMessage("command.no-permission"));
            return;
        }
        core.reload();
        context.sendMessage(localeService.getMessage(NAMESPACE, "command.reload-success"));
    }
}