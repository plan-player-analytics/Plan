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
package com.djrapitops.plan.addons.placeholderapi.placeholders;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.UUID;

/**
 * Placeholders about a servers.
 *
 * @author aidn5, Rsl1122
 */
public class ServerPlaceHolders extends AbstractPlanPlaceHolder {

    private final DBSystem dbSystem;
    private Formatter<Double> decimals;
    private Formatter<Double> percentage;

    public ServerPlaceHolders(
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        super(serverInfo);
        this.dbSystem = dbSystem;
        decimals = formatters.decimals();
        percentage = formatters.percentage();
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        Database database = dbSystem.getDatabase();
        UUID serverUUID = serverUUID();
        Serializable got = get(params, database, serverUUID);
        return got != null ? got.toString() : null;
    }

    // Checkstyle.OFF: CyclomaticComplexity

    public Serializable get(String params, Database database, UUID serverUUID) {
        switch (params.toLowerCase()) {
            case "server_tps_day":
                return decimals.apply(database.query(TPSQueries.averageTPS(dayAgo(), now(), serverUUID)));
            case "server_tps_week":
                return decimals.apply(database.query(TPSQueries.averageTPS(weekAgo(), now(), serverUUID)));
            case "server_tps_month":
                return decimals.apply(database.query(TPSQueries.averageTPS(monthAgo(), now(), serverUUID)));

            case "server_cpu_day":
                return percentage.apply(database.query(TPSQueries.averageCPU(dayAgo(), now(), serverUUID)));
            case "server_cpu_week":
                return percentage.apply(database.query(TPSQueries.averageCPU(weekAgo(), now(), serverUUID)));
            case "server_cpu_month":
                return percentage.apply(database.query(TPSQueries.averageCPU(monthAgo(), now(), serverUUID)));

            case "server_ram_day":
                return database.query(TPSQueries.averageRAM(dayAgo(), now(), serverUUID)) + " MB";
            case "server_ram_week":
                return database.query(TPSQueries.averageRAM(weekAgo(), now(), serverUUID)) + " MB";
            case "server_ram_month":
                return database.query(TPSQueries.averageRAM(monthAgo(), now(), serverUUID)) + " MB";

            case "server_chunks_day":
                return database.query(TPSQueries.averageChunks(dayAgo(), now(), serverUUID));
            case "server_chunks_week":
                return database.query(TPSQueries.averageChunks(weekAgo(), now(), serverUUID));
            case "server_chunks_month":
                return database.query(TPSQueries.averageChunks(monthAgo(), now(), serverUUID));

            case "server_entities_day":
                return database.query(TPSQueries.averageEntities(dayAgo(), now(), serverUUID));
            case "server_entities_week":
                return database.query(TPSQueries.averageEntities(weekAgo(), now(), serverUUID));
            case "server_entities_month":
                return database.query(TPSQueries.averageEntities(monthAgo(), now(), serverUUID));

            case "server_max_free_disk_day":
                return database.query(TPSQueries.maxFreeDisk(dayAgo(), now(), serverUUID));
            case "server_max_free_disk_week":
                return database.query(TPSQueries.maxFreeDisk(weekAgo(), now(), serverUUID));
            case "server_max_free_disk_month":
                return database.query(TPSQueries.maxFreeDisk(monthAgo(), now(), serverUUID));

            case "server_min_free_disk_day":
                return database.query(TPSQueries.minFreeDisk(dayAgo(), now(), serverUUID));
            case "server_min_free_disk_week":
                return database.query(TPSQueries.minFreeDisk(weekAgo(), now(), serverUUID));
            case "server_min_free_disk_month":
                return database.query(TPSQueries.minFreeDisk(monthAgo(), now(), serverUUID));

            case "server_average_free_disk_day":
                return database.query(TPSQueries.averageFreeDisk(dayAgo(), now(), serverUUID));
            case "server_average_free_disk_week":
                return database.query(TPSQueries.averageFreeDisk(weekAgo(), now(), serverUUID));
            case "server_average_free_disk_month":
                return database.query(TPSQueries.averageFreeDisk(monthAgo(), now(), serverUUID));

            case "server_name":
                return serverInfo.getServer().getName();
            case "server_uuid":
                return serverInfo.getServerUUID();

            default:
                return null;
        }
    }

    // Checkstyle.ON: CyclomaticComplexity
}
