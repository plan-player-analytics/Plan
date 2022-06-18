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
package com.djrapitops.plan.settings.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class responsible for generating and generating settings for DataExtensions to the config.
 *
 * @author AuroraLS3
 */
public class ExtensionSettings {

    private final PlanConfig config;

    public ExtensionSettings(
            PlanConfig config
    ) {
        this.config = config;
    }

    public boolean hasSection(String pluginName) {
        ConfigNode section = getPluginsSection();
        return section.getNode(pluginName + ".Enabled").isPresent();
    }

    private ConfigNode getPluginsSection() {
        return config.getNode("Plugins")
                .orElse(config.addNode("Plugins"));
    }

    public void createSection(String pluginName) throws IOException {
        ConfigNode section = getPluginsSection();

        section.set(pluginName + ".Enabled", true);
        section.sort();
        section.save();
    }

    public boolean isEnabled(String pluginName) {
        ConfigNode section = getPluginsSection();
        return section.getBoolean(pluginName + ".Enabled");
    }

    public void setEnabled(String pluginName, boolean value) {
        getPluginsSection().set(pluginName + ".Enabled", value);
    }

    public Set<String> getDisabled() {
        ConfigNode section = getPluginsSection();

        Set<String> disabledPlugins = new HashSet<>();
        for (ConfigNode plugin : section.getChildren()) {
            if (plugin.contains("Enabled") && !plugin.getBoolean("Enabled")) {
                disabledPlugins.add(plugin.getKey(false));
            }
        }
        return disabledPlugins;
    }
}
