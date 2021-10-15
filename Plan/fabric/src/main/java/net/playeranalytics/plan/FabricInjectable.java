/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
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
