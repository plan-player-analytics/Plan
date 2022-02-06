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
package com.djrapitops.plan.delivery.domain.datatransfer.extension;

import com.djrapitops.plan.extension.implementation.results.ExtensionData;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExtensionsDto {

    private final String playerUUID;
    private final String serverUUID;
    private final String serverName;
    private final List<ExtensionDataDto> extensionData;

    public ExtensionsDto(String playerUUID, String serverUUID, String serverName, List<ExtensionData> extensionData) {
        this.playerUUID = playerUUID;
        this.serverUUID = serverUUID;
        this.serverName = serverName;
        this.extensionData = extensionData.stream().map(ExtensionDataDto::new).collect(Collectors.toList());
    }

    public String getServerUUID() {
        return serverUUID;
    }

    public String getServerName() {
        return serverName;
    }

    public List<ExtensionDataDto> getExtensionData() {
        return extensionData;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionsDto that = (ExtensionsDto) o;
        return Objects.equals(getPlayerUUID(), that.getPlayerUUID()) && Objects.equals(getServerUUID(), that.getServerUUID()) && Objects.equals(getServerName(), that.getServerName()) && Objects.equals(getExtensionData(), that.getExtensionData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerUUID(), getServerUUID(), getServerName(), getExtensionData());
    }

    @Override
    public String toString() {
        return "ExtensionsDto{" +
                "playerUUID='" + playerUUID + '\'' +
                ", serverUUID='" + serverUUID + '\'' +
                ", serverName='" + serverName + '\'' +
                ", extensionData=" + extensionData +
                '}';
    }
}
