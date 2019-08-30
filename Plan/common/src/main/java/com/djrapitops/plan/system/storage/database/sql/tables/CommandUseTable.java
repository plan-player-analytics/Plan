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
package com.djrapitops.plan.system.storage.database.sql.tables;

import com.djrapitops.plan.system.storage.database.DBType;
import com.djrapitops.plan.system.storage.database.sql.parsing.CreateTableParser;
import com.djrapitops.plan.system.storage.database.sql.parsing.Sql;

import static com.djrapitops.plan.system.storage.database.sql.parsing.Sql.AND;
import static com.djrapitops.plan.system.storage.database.sql.parsing.Sql.WHERE;

/**
 * Table information about 'plan_commandusages'.
 *
 * Patches affecting this table:
 * {@link com.djrapitops.plan.system.storage.database.transactions.patches.Version10Patch}
 *
 * @author Rsl1122
 */
public class CommandUseTable {

    public static final String TABLE_NAME = "plan_commandusages";

    public static final String ID = "id";
    public static final String SERVER_ID = "server_id";
    public static final String COMMAND = "command";
    public static final String TIMES_USED = "times_used";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + COMMAND + ','
            + TIMES_USED + ','
            + SERVER_ID
            + ") VALUES (?, ?, " + ServerTable.STATEMENT_SELECT_SERVER_ID + ')';

    public static final String UPDATE_STATEMENT = "UPDATE " + CommandUseTable.TABLE_NAME + " SET "
            + CommandUseTable.TIMES_USED + "=" + CommandUseTable.TIMES_USED + "+ 1" +
            WHERE + CommandUseTable.SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID +
            AND + CommandUseTable.COMMAND + "=?";

    private CommandUseTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(COMMAND, Sql.varchar(20)).notNull()
                .column(TIMES_USED, Sql.INT).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.SERVER_ID)
                .toString();
    }
}
