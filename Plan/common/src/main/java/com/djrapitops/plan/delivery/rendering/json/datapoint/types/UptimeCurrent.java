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
package com.djrapitops.plan.delivery.rendering.json.datapoint.types;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.rendering.json.datapoint.SupportedFilters;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.gathering.ServerUptimeCalculator;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Datapoint for looking up current uptime of the servers.
 *
 * @author AuroraLS3
 */
@Singleton
public class UptimeCurrent implements Datapoint<Long> {

    private final ServerInfo serverInfo;
    private final ServerUptimeCalculator serverUptimeCalculator;

    @Inject
    public UptimeCurrent(ServerInfo serverInfo, ServerUptimeCalculator serverUptimeCalculator) {
        this.serverInfo = serverInfo;
        this.serverUptimeCalculator = serverUptimeCalculator;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.noPlayer();
    }

    @Override
    public Optional<Long> getValue(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            throw new BadRequestException("UPTIME_CURRENT does not support player parameter");
        }

        List<ServerUUID> serverUUIDs = filter.getServerUUIDs();
        ServerUUID serverUUID = serverUUIDs.size() == 1 ? serverUUIDs.get(0) : serverInfo.getServerUUID();

        return serverUptimeCalculator.getServerUptimeMillis(serverUUID);
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_UPTIME_CURRENT;
        } else {
            return WebPermission.DATA_NETWORK_UPTIME_CURRENT;
        }
    }

    @Override
    public DatapointType getType() {
        return DatapointType.UPTIME_CURRENT;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.TIME_AMOUNT;
    }
}
