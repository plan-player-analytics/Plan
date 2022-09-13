package net.playeranalytics.plugin;

import net.playeranalytics.plugin.information.StandalonePluginInformation;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.StandaloneRunnableFactory;
import net.playeranalytics.plugin.server.JavaUtilPluginLogger;
import net.playeranalytics.plugin.server.Listeners;
import net.playeranalytics.plugin.server.PluginLogger;

import java.util.logging.Logger;

public class StandalonePlatformAbstractionLayer implements PlatformAbstractionLayer {

    private final PluginLogger logger;
    private final StandaloneRunnableFactory runnableFactory;
    private final StandalonePluginInformation pluginInformation;
    private final Listeners listeners;

    public StandalonePlatformAbstractionLayer(Logger logger) {
        this.logger = new JavaUtilPluginLogger(logger);
        runnableFactory = new StandaloneRunnableFactory();
        pluginInformation = new StandalonePluginInformation();
        listeners = new Listeners() {
            @Override
            public void registerListener(Object o) {/*no-op*/}

            @Override
            public void unregisterListener(Object o) {/*no-op*/}

            @Override
            public void unregisterListeners() {/*no-op*/}
        };
    }

    @Override
    public PluginLogger getPluginLogger() {
        return logger;
    }

    @Override
    public Listeners getListeners() {
        return listeners;
    }

    @Override
    public RunnableFactory getRunnableFactory() {
        return runnableFactory;
    }

    @Override
    public PluginInformation getPluginInformation() {
        return pluginInformation;
    }
}
