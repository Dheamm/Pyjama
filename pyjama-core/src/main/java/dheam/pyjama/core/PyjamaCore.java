package dheam.pyjama.core;

import dheam.pyjama.api.platform.PlatformBridge;
import dheam.pyjama.core.command.CommandHandlers;
import dheam.pyjama.core.service.config.ConfigService;
import dheam.pyjama.core.service.locale.LocaleService;
import dheam.pyjama.core.service.version.VersionService;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

public final class PyjamaCore {

    private final Path dataFolder;
    private final PlatformBridge platformBridge;
    private final Logger logger;
    private ConfigService configService;
    private LocaleService localeService;
    private VersionService versionService;
    private CommandHandlers commandHandlers;
    private String buildName;

    public PyjamaCore(Path dataFolder, PlatformBridge platformBridge, Logger logger) {
        this.dataFolder = dataFolder;
        this.platformBridge = platformBridge;
        this.logger = logger;
    }

    public void enable() {
        configService = new ConfigService(dataFolder, logger);
        localeService = new LocaleService(dataFolder, logger, configService.getLocale());
        versionService = new VersionService(logger);
        commandHandlers = new CommandHandlers(this, localeService);
        buildName = versionService.getBuildName(platformBridge.platformId());
        registerGlobalPlaceholders();
        logger.info("Pyjama enabled.");
    }

    public void disable() {
        logger.info("Pyjama disabled.");
    }

    public void reload() {
        configService.reload();
        localeService.reload(configService.getLocale());
        registerGlobalPlaceholders();
    }

    private void registerGlobalPlaceholders() {
        localeService.setGlobalPlaceholders(Map.of(
                "command_name", configService.getCommandName(),
                "version", buildName
        ));
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public LocaleService getLocaleService() {
        return localeService;
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