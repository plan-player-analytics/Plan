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
public class WorldTimePlaceHolder extends AbstractPlanPlaceHolder {

    private Formatter<Long> timeAmount;

    public WorldTimePlaceHolder(
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        super(serverInfo, dbSystem);
        timeAmount = formatters.timeAmount();
    }

    public void register() {
        PlanPlaceholders.registerRaw("worlds_playtime_total_", (input, p) -> {
            String worldName = input.substring(22);

            WorldTimes worldTimes = dbSystem.getDatabase().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID()));

            return timeAmount.apply(worldTimes.getWorldPlaytime(worldName));
        });

        PlanPlaceholders.registerStatic("worlds_playtime_total", params -> {
            if (params.isEmpty()) {
                return null;
            }

            String worldName = params.get(0);

            WorldTimes worldTimes = dbSystem.getDatabase().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID()));

            return timeAmount.apply(worldTimes.getWorldPlaytime(worldName));
        });
    }
}
