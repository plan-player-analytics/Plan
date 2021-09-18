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

import net.fabricmc.loader.api.FabricLoader;
import net.playeranalytics.plan.PlanFabric;

import java.io.File;
import java.io.InputStream;

public class FabricPluginInformation implements PluginInformation {

    private final PlanFabric plugin;

    public FabricPluginInformation(PlanFabric plugin) {
        this.plugin = plugin;
    }

    @Override
    public InputStream getResourceFromJar(String resource) {
        return this.getClass().getResourceAsStream("/" + resource);
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public String getVersion() {
        return FabricLoader.getInstance()
                .getModContainer("plan")
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();
    }
}
