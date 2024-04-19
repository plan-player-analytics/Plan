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

import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Insert;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.building.Update;
import org.apache.commons.text.TextStringBuilder;
import org.intellij.lang.annotations.Language;

import java.util.Collection;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about 'plan_servers'.
 *
 * @author AuroraLS3
 * @see Server
 */
public class ServerTable {

    public static final String TABLE_NAME = "plan_servers";

    public static final String ID = "id";
    public static final String SERVER_UUID = "uuid";
    public static final String NAME = "name";
    public static final String WEB_ADDRESS = "web_address";
    public static final String INSTALLED = "is_installed";
    public static final String PROXY = "is_proxy";
    public static final String PLAN_VERSION = "plan_version";

    public static final String INSERT_STATEMENT = Insert.values(TABLE_NAME,
            SERVER_UUID, NAME,
            WEB_ADDRESS, INSTALLED, PROXY, PLAN_VERSION);

    public static final String UPDATE_STATEMENT = Update.values(TABLE_NAME,
                    SERVER_UUID,
                    NAME,
                    WEB_ADDRESS,
                    INSTALLED,
                    PROXY,
                    PLAN_VERSION)
            .where(SERVER_UUID + "=?")
            .toString();

    @Language("SQL")
    public static final String SELECT_SERVER_ID =
            '(' + SELECT + TABLE_NAME + '.' + ID +
                    FROM + TABLE_NAME +
                    WHERE + TABLE_NAME + '.' + SERVER_UUID + "=?" + LIMIT + "1)";

    private ServerTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(SERVER_UUID, Sql.varchar(36)).notNull().unique()
                .column(NAME, Sql.varchar(100))
                .column(WEB_ADDRESS, Sql.varchar(100))
                .column(INSTALLED, Sql.BOOL).notNull().defaultValue(true)
                .column(PROXY, Sql.BOOL).notNull().defaultValue(false)
                .column(PLAN_VERSION, Sql.varchar(18))
                .toString();
    }

    public static String selectServerIds(Collection<ServerUUID> serverUUIDs) {
        return '(' + SELECT + TABLE_NAME + '.' + ID +
                FROM + TABLE_NAME +
                WHERE + TABLE_NAME + '.' + SERVER_UUID + " IN ('" +
                new TextStringBuilder().appendWithSeparators(serverUUIDs, "','").build() +
                "'))";
    }
}
