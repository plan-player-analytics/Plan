package net.playeranalytics.plugin;

import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.JavaUtilPluginLogger;
import net.playeranalytics.plugin.server.Listeners;
import net.playeranalytics.plugin.server.PluginLogger;

import java.util.logging.Logger;

public class StandalonePlatformAbstractionLayer implements PlatformAbstractionLayer {

    private final PluginLogger logger;

    public StandalonePlatformAbstractionLayer(Logger logger) {this.logger = new JavaUtilPluginLogger(logger);}

    @Override
    public PluginLogger getPluginLogger() {
        return logger;
    }

    @Override
    public Listeners getListeners() {
        return new Listeners() {
            @Override
            public void registerListener(Object o) {
            }

            @Override
            public void unregisterListener(Object o) {
            }

            @Override
            public void unregisterListeners() {
            }
        };
    }

    @Override
    public RunnableFactory getRunnableFactory() {
        return null;
    }

    @Override
    public PluginInformation getPluginInformation() {
        return null;
    }
}
