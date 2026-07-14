package dheam.pyjama.core;

import dheam.pyjama.api.platform.PlatformBridge;
import dheam.pyjama.core.service.config.ConfigService;
import dheam.pyjama.core.service.locale.LocaleService;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class PyjamaCore {

    private final Path dataFolder;
    private final PlatformBridge platformBridge;
    private final Logger logger;
    private ConfigService configService;
    private LocaleService localeService;

    public PyjamaCore(Path dataFolder, PlatformBridge platformBridge, Logger logger) {
        this.dataFolder = dataFolder;
        this.platformBridge = platformBridge;
        this.logger = logger;
    }

    public void enable() {
        configService = new ConfigService(dataFolder, logger);
        localeService = new LocaleService(dataFolder, logger, configService.getLocale());
        logger.info("Pyjama enabled.");
    }

    public void disable() {
        logger.info("Pyjama disabled.");
    }

    public void reload() {
        configService.reload();
        localeService.reload(configService.getLocale());
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public LocaleService getLocaleService() {
        return localeService;
    }

    public PlatformBridge getPlatformBridge() {
        return platformBridge;
    }
}