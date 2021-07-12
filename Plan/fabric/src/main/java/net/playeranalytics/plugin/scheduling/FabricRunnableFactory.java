package net.playeranalytics.plugin.scheduling;

import net.fabricmc.api.DedicatedServerModInitializer;

public class FabricRunnableFactory implements RunnableFactory {

    private final DedicatedServerModInitializer plugin;

    public FabricRunnableFactory(DedicatedServerModInitializer plugin) {
        this.plugin = plugin;
    }

    @Override
    public UnscheduledTask create(Runnable runnable) {
        return null;
    }

    @Override
    public UnscheduledTask create(PluginRunnable pluginRunnable) {
        return null;
    }

    @Override
    public void cancelAllKnownTasks() {

    }
}
