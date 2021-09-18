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

import net.playeranalytics.plan.PlanFabric;
import net.playeranalytics.plugin.scheduling.FabricRunnableFactory;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.FabricListeners;
import net.playeranalytics.plugin.server.FabricPluginLogger;
import net.playeranalytics.plugin.server.Listeners;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.logging.log4j.LogManager;

public class FabricPlatformLayer implements PlatformAbstractionLayer {

    private final PlanFabric plugin;

    private PluginLogger pluginLogger;
    private Listeners listeners;
    private PluginInformation pluginInformation;
    private RunnableFactory runnableFactory;

    public FabricPlatformLayer(PlanFabric plugin) {
        this.plugin = plugin;
    }

    @Override
    public PluginLogger getPluginLogger() {
        if (pluginLogger == null) {
            pluginLogger = new FabricPluginLogger(LogManager.getLogger("Plan"));
        }
        return pluginLogger;
    }

    @Override
    public Listeners getListeners() {
        if (this.listeners == null) {
            this.listeners = new FabricListeners();
        }
        return listeners;
    }

    @Override
    public RunnableFactory getRunnableFactory() {
        if (this.runnableFactory == null) {
            this.runnableFactory = new FabricRunnableFactory();
        }
        return runnableFactory;
    }

    @Override
    public PluginInformation getPluginInformation() {
        if (this.pluginInformation == null) {
            this.pluginInformation = new FabricPluginInformation(this.plugin);
        }
        return pluginInformation;
    }
}
