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
package com.djrapitops.plan.delivery.domain.datatransfer;

import com.djrapitops.plan.identification.Server;
import org.jetbrains.annotations.NotNull;

/**
 * Represents outgoing server information json.
 */
public record ServerDto(String serverUUID, String serverName, boolean proxy) implements Comparable<ServerDto> {

    public static ServerDto fromServer(Server server) {
        return new ServerDto(server.getUuid().toString(), server.getIdentifiableName(), server.isProxy());
    }

    @Override
    public int compareTo(@NotNull ServerDto other) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.serverName, other.serverName);
    }
}
