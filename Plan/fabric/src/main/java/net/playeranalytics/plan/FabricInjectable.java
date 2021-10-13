package net.playeranalytics.plan;

import io.github.slimjar.injector.loader.Injectable;
import net.fabricmc.loader.launch.common.FabricLauncher;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.playeranalytics.plugin.server.FabricPluginLogger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Custom {@link Injectable} implementation for Fabric.
 * Appends dependencies to the classpath via Fabric's own launcher.
 */
public class FabricInjectable implements Injectable {

    private final FabricLauncher launcher;
    private final FabricPluginLogger pluginLogger;

    public FabricInjectable(FabricPluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
        this.launcher = FabricLauncherBase.getLauncher();
    }

    @Override
    public void inject(final URL url) throws IOException, InvocationTargetException, IllegalAccessException, URISyntaxException {
        pluginLogger.info("Proposed " + Paths.get(url.toURI()).getFileName().toString() + " to classpath");
        launcher.propose(url);
    }
}
