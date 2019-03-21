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
package com.djrapitops.plan.extension.implementation.results.player;

import com.djrapitops.plan.extension.icon.Icon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents data of a single extension about a player.
 *
 * @author Rsl1122
 */
public class ExtensionPlayerData {

    private String pluginName;
    private Icon pluginIcon;

    private List<ExtensionTabData> tabs;

    public ExtensionPlayerData(String pluginName, Icon pluginIcon) {
        this.pluginName = pluginName;
        this.pluginIcon = pluginIcon;

        tabs = new ArrayList<>();
    }

    public List<ExtensionTabData> getTabs() {
        return tabs;
    }

    public class Factory {

        private final ExtensionPlayerData data;

        public Factory() {
            data = new ExtensionPlayerData(pluginName, pluginIcon);
        }

        public Factory addTab(ExtensionTabData tab) {
            data.tabs.add(tab);
            return this;
        }

        public ExtensionPlayerData build() {
            Collections.sort(data.tabs);
            return data;
        }
    }

}