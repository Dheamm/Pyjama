package dheam.pyjama.core;

import dheam.pyjama.api.placeholder.PlaceholderBridge;
import dheam.pyjama.api.platform.PlatformBridge;
import dheam.pyjama.core.command.CommandHandlers;
import dheam.pyjama.core.service.config.ConfigService;
import dheam.pyjama.core.service.locale.LocaleService;
import dheam.pyjama.core.service.placeholder.PlaceholderService;
import dheam.pyjama.core.service.placeholder.Placeholders;
import dheam.pyjama.core.service.version.VersionService;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class PyjamaCore {

    private final Path dataFolder;
    private final PlatformBridge platformBridge;
    private final PlaceholderBridge placeholderBridge;
    private final Logger logger;
    private ConfigService configService;
    private LocaleService localeService;
    private VersionService versionService;
    private PlaceholderService placeholderService;
    private CommandHandlers commandHandlers;
    private String buildName;

    public PyjamaCore(Path dataFolder, PlatformBridge platformBridge, PlaceholderBridge placeholderBridge, Logger logger) {
        this.dataFolder = dataFolder;
        this.platformBridge = platformBridge;
        this.placeholderBridge = placeholderBridge;
        this.logger = logger;
    }

    public void enable() {
        configService = new ConfigService(dataFolder, logger);
        versionService = new VersionService(logger);
        buildName = versionService.getBuildName(platformBridge.platformId());
        placeholderService = new PlaceholderService();
        registerGlobalPlaceholders();
        localeService = new LocaleService(dataFolder, logger, configService.getLocale(), placeholderService, placeholderBridge);
        commandHandlers = new CommandHandlers(this, localeService, placeholderService);
        logger.info("Pyjama enabled.");
    }

    public void disable() {
        logger.info("Pyjama disabled.");
    }

    public void reload() {
        configService.reload();
        registerGlobalPlaceholders();
        localeService.reload(configService.getLocale());
    }

    private void registerGlobalPlaceholders() {
        placeholderService.registerGlobal(Placeholders.COMMAND_NAME, () -> configService.getCommandName());
        placeholderService.registerGlobal(Placeholders.VERSION, () -> buildName);
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public LocaleService getLocaleService() {
        return localeService;
    }

    public PlaceholderService getPlaceholderService() {
        return placeholderService;
    }

    public CommandHandlers getCommandHandlers() {
        return commandHandlers;
    }

    public PlatformBridge getPlatformBridge() {
        return platformBridge;
    }

    public String getBuildName() {
        return buildName;
    }
}