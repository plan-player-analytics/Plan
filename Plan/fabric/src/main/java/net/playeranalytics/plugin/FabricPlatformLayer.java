package net.playeranalytics.plugin;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.FabricListeners;
import net.playeranalytics.plugin.server.FabricPluginLogger;
import net.playeranalytics.plugin.server.Listeners;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.logging.log4j.LogManager;

public class FabricPlatformLayer implements PlatformAbstractionLayer {

    private final DedicatedServerModInitializer plugin;

    private PluginLogger pluginLogger;
    private Listeners listeners;
    private PluginInformation pluginInformation;

    public FabricPlatformLayer(DedicatedServerModInitializer plugin) {
        this.plugin = plugin;
    }

    @Override
    public PluginLogger getPluginLogger() {
        if (pluginLogger == null) {
            pluginLogger = new FabricPluginLogger(LogManager.getLogger());
        }
        return pluginLogger;
    }

    @Override
    public Listeners getListeners() {
        if (this.listeners == null) { this.listeners = new FabricListeners(this.plugin); }
        return listeners;
    }

    @Override
    public RunnableFactory getRunnableFactory() {
        return null;
    }

    @Override
    public PluginInformation getPluginInformation() {
        if (this.pluginInformation == null) { this.pluginInformation = new FabricPluginInformation(this.plugin); }
        return null;
    }
}
