package dheam.pyjama.core;

import dheam.pyjama.core.service.config.ConfigService;
import dheam.pyjama.core.service.locale.LocaleService;

import java.io.File;
import java.util.logging.Logger;

public final class PyjamaCore {

    private final File dataFolder;
    private final Logger logger;
    private ConfigService configService;
    private LocaleService localeService;

    public PyjamaCore(File dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
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

    public ConfigService getConfigService() {
        return configService;
    }

    public LocaleService getLocaleService() {
        return localeService;
    }
}