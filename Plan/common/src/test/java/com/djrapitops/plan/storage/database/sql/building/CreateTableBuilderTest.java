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
package com.djrapitops.plan.storage.database.sql.building;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link CreateTableBuilder}.
 *
 * @author AuroraLS3
 */
class CreateTableBuilderTest {

    @Test
    void createsSameTablesAsOldParser() {
        String expected = "CREATE TABLE IF NOT EXISTS plan_servers (id integer NOT NULL AUTO_INCREMENT,uuid varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL UNIQUE,name varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,web_address varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,is_installed boolean NOT NULL DEFAULT 1,max_players integer NOT NULL DEFAULT -1,PRIMARY KEY (id))";
        String result = CreateTableBuilder.create(ServerTable.TABLE_NAME, DBType.MYSQL)
                .column(ServerTable.ID, Sql.INT)
                .primaryKey()
                .column(ServerTable.SERVER_UUID, Sql.varchar(36)).notNull().unique()
                .column(ServerTable.NAME, Sql.varchar(100))
                .column(ServerTable.WEB_ADDRESS, Sql.varchar(100))
                .column(ServerTable.INSTALLED, Sql.BOOL).notNull().defaultValue(true)
                .column("max_players", Sql.INT).notNull().defaultValue("-1")
                .toString();
        assertEquals(expected, result);
    }

}