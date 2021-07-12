package net.playeranalytics.plugin.server;

import net.fabricmc.api.DedicatedServerModInitializer;

public class FabricListeners implements Listeners {

    private final DedicatedServerModInitializer plugin;

    public FabricListeners(DedicatedServerModInitializer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerListener(Object o) {

    }

    @Override
    public void unregisterListener(Object o) {

    }

    @Override
    public void unregisterListeners() {

    }
}
