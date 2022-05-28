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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.TPSTable;
import com.djrapitops.plan.storage.database.sql.tables.WorldTable;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.java.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Static method class for queries that use large amount of memory.
 *
 * @author AuroraLS3
 */
public class LargeFetchQueries {

    private LargeFetchQueries() {
        /* Static method class */
    }

    /**
     * Query database for TPS data.
     *
     * @return Map: Server UUID - List of TPS data
     */
    public static Query<Map<ServerUUID, List<TPS>>> fetchAllTPSData() {
        String serverIDColumn = ServerTable.TABLE_NAME + '.' + ServerTable.ID;
        String serverUUIDColumn = ServerTable.TABLE_NAME + '.' + ServerTable.SERVER_UUID + " as s_uuid";
        String sql = SELECT +
                TPSTable.DATE + ',' +
                TPSTable.TPS + ',' +
                TPSTable.PLAYERS_ONLINE + ',' +
                TPSTable.CPU_USAGE + ',' +
                TPSTable.RAM_USAGE + ',' +
                TPSTable.ENTITIES + ',' +
                TPSTable.CHUNKS + ',' +
                TPSTable.FREE_DISK + ',' +
                serverUUIDColumn +
                FROM + TPSTable.TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " on " + serverIDColumn + "=" + TPSTable.SERVER_ID;

        return new QueryAllStatement<Map<ServerUUID, List<TPS>>>(sql, 50000) {
            @Override
            public Map<ServerUUID, List<TPS>> processResults(ResultSet set) throws SQLException {
                Map<ServerUUID, List<TPS>> serverMap = new HashMap<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString("s_uuid"));

                    List<TPS> tpsList = serverMap.computeIfAbsent(serverUUID, Lists::create);

                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(TPSTable.DATE))
                            .tps(set.getDouble(TPSTable.TPS))
                            .playersOnline(set.getInt(TPSTable.PLAYERS_ONLINE))
                            .usedCPU(set.getDouble(TPSTable.CPU_USAGE))
                            .usedMemory(set.getLong(TPSTable.RAM_USAGE))
                            .entities(set.getInt(TPSTable.ENTITIES))
                            .chunksLoaded(set.getInt(TPSTable.CHUNKS))
                            .freeDiskSpace(set.getLong(TPSTable.FREE_DISK))
                            .toTPS();

                    tpsList.add(tps);
                }
                return serverMap;
            }
        };
    }

    /**
     * Query database for world names.
     *
     * @return Map: Server UUID - List of world names
     */
    public static Query<Map<ServerUUID, Collection<String>>> fetchAllWorldNames() {
        String sql = SELECT + '*' + FROM + WorldTable.TABLE_NAME;

        return new QueryAllStatement<Map<ServerUUID, Collection<String>>>(sql, 1000) {
            @Override
            public Map<ServerUUID, Collection<String>> processResults(ResultSet set) throws SQLException {
                Map<ServerUUID, Collection<String>> worldMap = new HashMap<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString(WorldTable.SERVER_UUID));
                    Collection<String> worlds = worldMap.computeIfAbsent(serverUUID, Maps::createSet);
                    worlds.add(set.getString(WorldTable.NAME));
                }
                return worldMap;
            }
        };
    }
}