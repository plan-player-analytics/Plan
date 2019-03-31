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

import com.djrapitops.plan.extension.implementation.results.ExtensionInformation;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents data of a single extension about a player.
 *
 * @author Rsl1122
 */
public class ExtensionPlayerData implements Comparable<ExtensionPlayerData> {

    private final int pluginID;

    private ExtensionInformation extensionInformation;

    private List<ExtensionTabData> tabs;

    private ExtensionPlayerData(int pluginID) {
        this.pluginID = pluginID;

        tabs = new ArrayList<>();
    }

    public int getPluginID() {
        return pluginID;
    }

    public ExtensionInformation getExtensionInformation() {
        return extensionInformation;
    }

    public boolean hasOnlyGenericTab() {
        return tabs.size() == 1 && tabs.get(0).getTabInformation().getTabName().isEmpty();
    }

    public List<ExtensionTabData> getTabs() {
        return tabs;
    }

    @Override
    public int compareTo(ExtensionPlayerData o) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.extensionInformation.getPluginName(), o.extensionInformation.getPluginName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionPlayerData)) return false;
        ExtensionPlayerData that = (ExtensionPlayerData) o;
        return pluginID == that.pluginID &&
                extensionInformation.equals(that.extensionInformation) &&
                tabs.equals(that.tabs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginID, extensionInformation, tabs);
    }

    public static class Factory {

        private final ExtensionPlayerData data;

        public Factory(int pluginId) {
            data = new ExtensionPlayerData(pluginId);
        }

        public Factory setInformation(ExtensionInformation information) {
            if (information.getId() != data.pluginID) {
                throw new IllegalArgumentException("ID mismatch, wanted id: " + data.pluginID + " but got " + information);
            }
            data.extensionInformation = information;
            return this;
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