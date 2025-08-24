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
