package dheam.pyjama.core.command;

import dheam.pyjama.core.PyjamaCore;
import dheam.pyjama.core.service.locale.LocaleService;
import dheam.pyjama.core.service.placeholder.PlaceholderService;

public final class CommandHandlers {

    private final RootCommandHandler rootCommandHandler;
    private final ReloadCommandHandler reloadCommandHandler;

    public CommandHandlers(PyjamaCore core, LocaleService localeService, PlaceholderService placeholderService) {
        this.rootCommandHandler = new RootCommandHandler(localeService);
        this.reloadCommandHandler = new ReloadCommandHandler(core, localeService, placeholderService);
    }

    public RootCommandHandler root() {
        return rootCommandHandler;
    }

    public ReloadCommandHandler reload() {
        return reloadCommandHandler;
    }
}