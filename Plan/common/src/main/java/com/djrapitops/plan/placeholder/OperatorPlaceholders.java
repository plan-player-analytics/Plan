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
package com.djrapitops.plan.placeholder;

import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Placeholders about operators.
 *
 * @author aidn5, AuroraLS3
 */
@Singleton
public class OperatorPlaceholders implements Placeholders {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    @Inject
    public OperatorPlaceholders(
            DBSystem dbSystem, ServerInfo serverInfo
    ) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
    }

    @Override
    public void register(PlanPlaceholders placeholders) {
        placeholders.registerStatic("operators_total",
                parameters -> dbSystem.getDatabase().query(PlayerCountQueries.operators(getServerUUID(parameters)))
        );
    }

    private ServerUUID getServerUUID(@Untrusted Arguments parameters) {
        return parameters.get(0).flatMap(this::getServerUUIDForServerIdentifier).orElseGet(serverInfo::getServerUUID);
    }

    private Optional<ServerUUID> getServerUUIDForServerIdentifier(@Untrusted String serverIdentifier) {
        return dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverIdentifier))
                .map(Server::getUuid);
    }
}
