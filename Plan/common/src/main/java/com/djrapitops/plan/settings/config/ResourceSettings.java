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

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;

public class ResourceSettings {

    private final PlanConfig config;

    public ResourceSettings(
            PlanConfig config
    ) {
        this.config = config;
    }

    public boolean shouldBeCustomized(String plugin, String fileName) {
        ConfigNode fileCustomization = getCustomizationConfigNode();
        fileCustomization.setComment(Collections.singletonList("The files are placed in /Plan/web/ if the setting is 'true' when accessed."));

        ConfigNode pluginCustomization = fileCustomization.getNode(plugin).orElseGet(() -> fileCustomization.addNode(plugin));
        String fileNameNonPath = StringUtils.replaceChars(fileName, '.', ',');

        if (pluginCustomization.contains(fileNameNonPath)) {
            return pluginCustomization.getBoolean(fileNameNonPath);
        } else {
            pluginCustomization.set(fileNameNonPath, false);
            try {
                pluginCustomization.sort();
                pluginCustomization.save();
            } catch (IOException e) {
                throw new UncheckedIOException("Could not save config.yml: " + e.getMessage(), e);
            }
            return false;
        }
    }

    public ConfigNode getCustomizationConfigNode() {
        return config.getNode("Customized_files").orElseGet(() -> config.addNode("Customized_files"));
    }

}
