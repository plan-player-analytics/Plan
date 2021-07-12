package net.playeranalytics.plugin;

import net.fabricmc.api.DedicatedServerModInitializer;

import java.io.File;
import java.io.InputStream;

public class FabricPluginInformation implements PluginInformation {

    private final DedicatedServerModInitializer plugin;

    public FabricPluginInformation(DedicatedServerModInitializer plugin) {
        this.plugin = plugin;
    }

    @Override
    public InputStream getResourceFromJar(String s) {
        return null;
    }

    @Override
    public File getDataFolder() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }
}
