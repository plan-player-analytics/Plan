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
package com.djrapitops.plan.db.access.queries.objects;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.tables.ServerTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.tables.TPSTable.*;

/**
 * Queries for {@link com.djrapitops.plan.data.container.TPS} objects.
 *
 * @author Rsl1122
 */
public class TPSQueries {

    private TPSQueries() {
        /* Static method class */
    }

    public static Query<List<TPS>> fetchTPSDataOfServer(UUID serverUUID) {
        String sql = Select.all(TABLE_NAME)
                .where(SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID)
                .toString();

        return new QueryStatement<List<TPS>>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<TPS> processResults(ResultSet set) throws SQLException {
                List<TPS> data = new ArrayList<>();
                while (set.next()) {

                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(DATE))
                            .tps(set.getDouble(TPS))
                            .playersOnline(set.getInt(PLAYERS_ONLINE))
                            .usedCPU(set.getDouble(CPU_USAGE))
                            .usedMemory(set.getLong(RAM_USAGE))
                            .entities(set.getInt(ENTITIES))
                            .chunksLoaded(set.getInt(CHUNKS))
                            .freeDiskSpace(set.getLong(FREE_DISK))
                            .toTPS();

                    data.add(tps);
                }
                return data;
            }
        };
    }

}