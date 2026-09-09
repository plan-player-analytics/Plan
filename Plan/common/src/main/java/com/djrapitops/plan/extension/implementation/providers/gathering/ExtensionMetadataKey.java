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
package com.djrapitops.plan.extension.implementation.providers.gathering;

import com.djrapitops.plan.identification.ServerUUID;

import java.util.Objects;

public final class ExtensionMetadataKey {
    private final boolean tableProvider;
    private final ServerUUID serverUUID;
    private final String pluginName;
    private final String providerName;

    public ExtensionMetadataKey(boolean tableProvider, ServerUUID serverUUID, String pluginName, String providerName) {
        this.tableProvider = tableProvider;
        this.serverUUID = serverUUID;
        this.pluginName = pluginName;
        this.providerName = providerName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ExtensionMetadataKey)) return false;
        ExtensionMetadataKey that = (ExtensionMetadataKey) other;
        return tableProvider == that.tableProvider &&
                Objects.equals(serverUUID, that.serverUUID) &&
                Objects.equals(pluginName, that.pluginName) &&
                Objects.equals(providerName, that.providerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableProvider, serverUUID, pluginName, providerName);
    }
}
