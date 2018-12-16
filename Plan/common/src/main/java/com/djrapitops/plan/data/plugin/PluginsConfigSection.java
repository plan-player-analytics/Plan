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
package com.djrapitops.plan.data.plugin;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.config.ConfigNode;

import java.io.IOException;

/**
 * Class responsible for generating and generating settings for PluginData
 * objects to the config.
 *
 * @author Rsl1122
 */
public class PluginsConfigSection {

    private final PlanConfig config;

    public PluginsConfigSection(
            PlanConfig config
    ) {
        this.config = config;
    }

    public boolean hasSection(PluginData dataSource) {
        ConfigNode section = getPluginsSection();
        String pluginName = dataSource.getSourcePlugin();
        return section.getChildren().containsKey(pluginName)
                && section.getConfigNode(pluginName).getChildren().containsKey("Enabled");
    }

    private ConfigNode getPluginsSection() {
        return config.getConfigNode("Plugins");
    }

    public void createSection(PluginData dataSource) throws IOException {
        ConfigNode section = getPluginsSection();
        String pluginName = dataSource.getSourcePlugin();

        section.set(pluginName + ".Enabled", true);
        section.sort();
        section.save();
    }

    public boolean isEnabled(PluginData dataSource) {
        ConfigNode section = getPluginsSection();

        String pluginName = dataSource.getSourcePlugin();
        return section.getBoolean(pluginName + ".Enabled");
    }
}
