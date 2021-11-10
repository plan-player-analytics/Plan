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

import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.WorldTimesQueries;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Placeholders about a world times.
 *
 * @author aidn5, AuroraLS3
 */
@Singleton
public class WorldTimePlaceHolders implements Placeholders {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Formatters formatters;

    @Inject
    public WorldTimePlaceHolders(
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.formatters = formatters;
    }

    @Override
    public void register(
            PlanPlaceholders placeholders
    ) {
        placeholders.registerRaw("worlds_playtime_total_", (input, p) -> {
            // get world total play time
            // e.g. "plan_worlds_playtime_total_%worldname%"
            // where %worldname% is "world_nether"
            String worldName = input.substring("worlds_playtime_total_".length());

            return getWorldPlaytime(worldName);
        });

        placeholders.registerStatic("worlds_playtime_total", params ->
                params.get(0).map(this::getWorldPlaytime).orElse("-"));
    }

    private String getWorldPlaytime(String worldName) {
        WorldTimes worldTimes = dbSystem.getDatabase().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverInfo.getServerUUID()));

        return formatters.timeAmount().apply(worldTimes.getWorldPlaytime(worldName));
    }
}
