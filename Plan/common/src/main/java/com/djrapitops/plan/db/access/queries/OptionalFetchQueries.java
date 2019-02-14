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
package com.djrapitops.plan.db.access.queries;

import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.ServerTable;
import com.djrapitops.plan.db.sql.tables.TPSTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Static method class for queries that return single item if found.
 *
 * @author Rsl1122
 */
public class OptionalFetchQueries {

    private OptionalFetchQueries() {
        /* Static method class */
    }

    public static Query<Optional<DateObj<Integer>>> fetchPeakPlayerCount(UUID serverUUID, long afterDate) {
        String sql = "SELECT " + TPSTable.DATE + ", MAX(" + TPSTable.PLAYERS_ONLINE + ") as max FROM " + TPSTable.TABLE_NAME +
                " WHERE " + TPSTable.SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID +
                " AND " + TPSTable.DATE + ">= ?" +
                " GROUP BY " + TPSTable.SERVER_ID;

        return new QueryStatement<Optional<DateObj<Integer>>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, afterDate);
            }

            @Override
            public Optional<DateObj<Integer>> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new DateObj<>(
                            set.getLong(TPSTable.DATE),
                            set.getInt(TPSTable.PLAYERS_ONLINE)
                    ));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Optional<DateObj<Integer>>> fetchAllTimePeakPlayerCount(UUID serverUUID) {
        return db -> db.query(fetchPeakPlayerCount(serverUUID, 0));
    }
}