package net.playeranalytics.plugin.server;

import org.apache.logging.log4j.Logger;

public class FabricPluginLogger implements PluginLogger {

    private final Logger logger;

    public FabricPluginLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public PluginLogger info(String message) {
        logger.info(message);
        return null;
    }

    @Override
    public PluginLogger warn(String message) {
        logger.warn(message);
        return null;
    }

    @Override
    public PluginLogger error(String message) {
        logger.error(message);
        return null;
    }

    @Override
    public PluginLogger warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
        return null;
    }

    @Override
    public PluginLogger error(String message, Throwable throwable) {
        logger.error(message, throwable);
        return null;
    }
}
