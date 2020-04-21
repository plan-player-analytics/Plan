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
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

import java.util.UUID;

import static com.djrapitops.plan.utilities.MiscUtils.*;

/**
 * Placeholders about a servers.
 *
 * @author aidn5, Rsl1122
 */
public class ServerPlaceHolders {

    public static void register(
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        Formatter<Double> decimals = formatters.decimals();
        Formatter<Double> percentage = formatters.percentage();

        Database database = dbSystem.getDatabase();
        UUID serverUUID = serverInfo.getServerUUID();

        PlanPlaceholders.registerStatic("server_tps_day",
                () -> decimals.apply(database.query(TPSQueries.averageTPS(dayAgo(), now(), serverUUID))));

        PlanPlaceholders.registerStatic("server_tps_week",
                () -> decimals.apply(database.query(TPSQueries.averageTPS(weekAgo(), now(), serverUUID))));

        PlanPlaceholders.registerStatic("server_tps_month",
                () -> decimals.apply(database.query(TPSQueries.averageTPS(monthAgo(), now(), serverUUID))));

        PlanPlaceholders.registerStatic("server_cpu_day",
                () -> percentage.apply(database.query(TPSQueries.averageCPU(dayAgo(), now(), serverUUID))));

        PlanPlaceholders.registerStatic("server_cpu_week",
                () -> percentage.apply(database.query(TPSQueries.averageCPU(weekAgo(), now(), serverUUID))));

        PlanPlaceholders.registerStatic("server_cpu_month",
                () -> percentage.apply(database.query(TPSQueries.averageCPU(monthAgo(), now(), serverUUID))));

        PlanPlaceholders.registerStatic("server_ram_day",
                () -> database.query(TPSQueries.averageRAM(dayAgo(), now(), serverUUID)) + " MB");

        PlanPlaceholders.registerStatic("server_ram_week",
                () -> database.query(TPSQueries.averageRAM(weekAgo(), now(), serverUUID)) + " MB");

        PlanPlaceholders.registerStatic("server_ram_month",
                () -> database.query(TPSQueries.averageRAM(monthAgo(), now(), serverUUID)) + " MB");

        PlanPlaceholders.registerStatic("server_chunks_day",
                () -> database.query(TPSQueries.averageChunks(dayAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_chunks_week",
                () -> database.query(TPSQueries.averageChunks(weekAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_chunks_month",
                () -> database.query(TPSQueries.averageChunks(monthAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_entities_day",
                () -> database.query(TPSQueries.averageEntities(dayAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_entities_week",
                () -> database.query(TPSQueries.averageEntities(weekAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_entities_month",
                () -> database.query(TPSQueries.averageEntities(monthAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_max_free_disk_day",
                () -> database.query(TPSQueries.maxFreeDisk(dayAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_max_free_disk_week",
                () -> database.query(TPSQueries.maxFreeDisk(weekAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_max_free_disk_month",
                () -> database.query(TPSQueries.maxFreeDisk(monthAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_min_free_disk_day",
                () -> database.query(TPSQueries.minFreeDisk(dayAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_min_free_disk_week",
                () -> database.query(TPSQueries.minFreeDisk(weekAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_min_free_disk_month",
                () -> database.query(TPSQueries.minFreeDisk(monthAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_average_free_disk_day",
                () -> database.query(TPSQueries.averageFreeDisk(dayAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_average_free_disk_week",
                () -> database.query(TPSQueries.averageFreeDisk(weekAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_average_free_disk_month",
                () -> database.query(TPSQueries.averageFreeDisk(monthAgo(), now(), serverUUID)));

        PlanPlaceholders.registerStatic("server_name",
                () -> serverInfo.getServer().getName());

        PlanPlaceholders.registerStatic("server_uuid",
                serverInfo::getServerUUID);

    }
}
