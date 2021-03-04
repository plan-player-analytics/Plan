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
package com.djrapitops.plan.storage.database.sql.tables;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.NicknameLastSeenPatch;
import com.djrapitops.plan.storage.database.transactions.patches.NicknamesOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;

import static com.djrapitops.plan.storage.database.sql.building.Sql.AND;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Table information about 'plan_nicknames'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link NicknameLastSeenPatch}
 * {@link NicknamesOptimizationPatch}
 *
 * @author AuroraLS3
 */
public class NicknamesTable {

    public static final String TABLE_NAME = "plan_nicknames";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    public static final String SERVER_UUID = "server_uuid";
    public static final String NICKNAME = "nickname";
    public static final String LAST_USED = "last_used";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            USER_UUID + ',' +
            SERVER_UUID + ',' +
            NICKNAME + ',' +
            LAST_USED +
            ") VALUES (?, ?, ?, ?)";

    public static final String UPDATE_STATEMENT = "UPDATE " + TABLE_NAME + " SET " + LAST_USED + "=?" +
            WHERE + NICKNAME + "=?" +
            AND + USER_UUID + "=?" +
            AND + SERVER_UUID + "=?";

    private NicknamesTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(NICKNAME, Sql.varchar(75)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(LAST_USED, Sql.LONG).notNull()
                .toString();
    }
}
