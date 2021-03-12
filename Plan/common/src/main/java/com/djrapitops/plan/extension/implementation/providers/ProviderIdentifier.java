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
package com.djrapitops.plan.extension.implementation.providers;

import com.djrapitops.plan.identification.ServerUUID;

import java.util.Objects;
import java.util.Optional;

public class ProviderIdentifier {
    private final ServerUUID serverUUID;
    private final String pluginName;
    private final String providerName;
    private String serverName;

    public ProviderIdentifier(ServerUUID serverUUID, String pluginName, String providerName) {
        this.serverUUID = serverUUID;
        this.pluginName = pluginName;
        this.providerName = providerName;
    }

    public Optional<String> getServerName() {
        return Optional.of(serverName);
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public ServerUUID getServerUUID() {
        return serverUUID;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getProviderName() {
        return providerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProviderIdentifier that = (ProviderIdentifier) o;
        return serverUUID.equals(that.serverUUID) && pluginName.equals(that.pluginName) && providerName.equals(that.providerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverUUID, pluginName, providerName);
    }
}
