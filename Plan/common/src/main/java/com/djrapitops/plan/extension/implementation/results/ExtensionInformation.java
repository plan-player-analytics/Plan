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

import com.djrapitops.plan.extension.icon.Icon;

/**
 * Information about a DataExtension stored in the database.
 *
 * @author AuroraLS3
 */
public class ExtensionInformation {

    private final int id;
    private final String pluginName;
    private final Icon icon;

    public ExtensionInformation(int id, String pluginName, Icon icon) {
        this.id = id;
        this.pluginName = pluginName;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getPluginName() {
        return pluginName;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return '{' + "id=" + id + ", pluginName='" + pluginName + '\'' + '}';
    }
}