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
package com.djrapitops.plan.extension.implementation.results;

import java.util.*;

/**
 * Represents data of a single extension about a server.
 *
 * @author AuroraLS3
 */
public class ExtensionData implements Comparable<ExtensionData> {

    private final int pluginID;

    private ExtensionInformation extensionInformation;

    private final Map<String, ExtensionTabData> tabs;

    private ExtensionData(int pluginID) {
        this.pluginID = pluginID;

        tabs = new HashMap<>();
    }

    public int getPluginID() {
        return pluginID;
    }

    public ExtensionInformation getExtensionInformation() {
        return extensionInformation;
    }

    public boolean hasOnlyGenericTab() {
        return tabs.size() == 1 && tabs.containsKey("");
    }

    public boolean doesNeedWiderSpace() {
        for (ExtensionTabData tab : tabs.values()) {
            for (ExtensionTableData table : tab.getTableData()) {
                if (table.isWideTable()) return true;
            }
        }
        return false;
    }

    public List<ExtensionTabData> getTabs() {
        List<ExtensionTabData> tabList = new ArrayList<>(tabs.values());
        Collections.sort(tabList);
        return tabList;
    }

    @Override
    public int compareTo(ExtensionData o) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.extensionInformation.getPluginName(), o.extensionInformation.getPluginName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionData)) return false;
        ExtensionData that = (ExtensionData) o;
        return pluginID == that.pluginID &&
                Objects.equals(extensionInformation, that.extensionInformation) &&
                Objects.equals(tabs, that.tabs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginID, extensionInformation, tabs);
    }

    @Override
    public String toString() {
        return "ExtensionData{" +
                "pluginID=" + pluginID +
                ", extensionInformation=" + extensionInformation +
                ", tabs=" + tabs +
                '}';
    }

    public static class Builder {

        private final ExtensionData data;

        public Builder(int pluginId) {
            data = new ExtensionData(pluginId);
        }

        public Builder combine(Builder with) {
            if (with != null) {
                for (ExtensionTabData tab : with.build().getTabs()) {
                    Optional<ExtensionTabData> found = getTab(tab.getTabInformation().getTabName());
                    if (found.isPresent()) {
                        found.get().combine(tab);
                    } else {
                        addTab(tab);
                    }
                }
            }
            return this;
        }

        public Builder setInformation(ExtensionInformation information) {
            if (information.getId() != data.pluginID) {
                throw new IllegalArgumentException("ID mismatch, wanted id: " + data.pluginID + " but got " + information);
            }
            data.extensionInformation = information;
            return this;
        }

        public Builder addTab(ExtensionTabData tab) {
            data.tabs.put(tab.getTabInformation().getTabName(), tab);
            return this;
        }

        public Optional<ExtensionTabData> getTab(String tabName) {
            return Optional.ofNullable(data.tabs.get(tabName));
        }

        public ExtensionData build() {
            return data;
        }
    }

}