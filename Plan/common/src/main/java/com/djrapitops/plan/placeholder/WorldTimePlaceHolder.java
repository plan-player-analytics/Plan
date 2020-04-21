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

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.WorldTimesQueries;

/**
 * Placeholders about a world times.
 *
 * @author aidn5, Rsl1122
 */
public class WorldTimePlaceHolder {

    public static void register(
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        Formatter<Long> timeAmount = formatters.timeAmount();

        PlanPlaceholders.registerRaw("worlds_playtime_total_", (input, p) -> {
            // get world total play time
            // e.g. "plan_worlds_playtime_total_%worldname%"
            // where %worldname% is "world_nether"
            String worldName = input.substring("worlds_playtime_total_".length());

            WorldTimes worldTimes = dbSystem.getDatabase().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverInfo.getServerUUID()));

            return timeAmount.apply(worldTimes.getWorldPlaytime(worldName));
        });

        PlanPlaceholders.registerStatic("worlds_playtime_total", params -> {
            if (params.isEmpty()) {
                return null;
            }

            String worldName = params.get(0);

            WorldTimes worldTimes = dbSystem.getDatabase().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverInfo.getServerUUID()));

            return timeAmount.apply(worldTimes.getWorldPlaytime(worldName));
        });
    }
}
