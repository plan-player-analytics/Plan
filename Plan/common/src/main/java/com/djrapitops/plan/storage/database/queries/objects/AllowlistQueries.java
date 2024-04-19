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
package com.djrapitops.plan.storage.database.queries.objects;

import com.djrapitops.plan.delivery.domain.datatransfer.AllowlistBounce;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.tables.AllowlistBounceTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import org.intellij.lang.annotations.Language;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Query against {@link AllowlistBounceTable}.
 *
 * @author AuroraLS3
 */
public class AllowlistQueries {

    private AllowlistQueries() {
        /* Static method class */
    }

    public static Query<List<AllowlistBounce>> getBounces(ServerUUID serverUUID) {
        @Language("SQL") String sql = SELECT +
                AllowlistBounceTable.UUID + ',' +
                AllowlistBounceTable.USER_NAME + ',' +
                AllowlistBounceTable.TIMES + ',' +
                AllowlistBounceTable.LAST_BOUNCE +
                FROM + AllowlistBounceTable.TABLE_NAME +
                WHERE + AllowlistBounceTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;
        return db -> db.queryList(sql, AllowlistQueries::extract, serverUUID);
    }

    private static AllowlistBounce extract(ResultSet set) throws SQLException {
        return new AllowlistBounce(
                UUID.fromString(set.getString(AllowlistBounceTable.UUID)),
                set.getString(AllowlistBounceTable.USER_NAME),
                set.getInt(AllowlistBounceTable.TIMES),
                set.getLong(AllowlistBounceTable.LAST_BOUNCE)
        );
    }
}
