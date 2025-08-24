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

/**
 * Table information about 'plan_tps'.
 *
 * @author AuroraLS3
 */
public class TPSTable {

    public static final String TABLE_NAME = "plan_tps";

    public static final String ID = "id";
    public static final String SERVER_ID = "server_id";
    public static final String DATE = "date";
    public static final String TPS = "tps";
    public static final String PLAYERS_ONLINE = "players_online";
    public static final String CPU_USAGE = "cpu_usage";
    public static final String RAM_USAGE = "ram_usage";
    public static final String ENTITIES = "entities";
    public static final String CHUNKS = "chunks_loaded";
    public static final String FREE_DISK = "free_disk_space";
    public static final String MSPT_AVERAGE = "mspt_average";
    public static final String MSPT_95TH_PERCENTILE = "mspt_95th_percentile";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + SERVER_ID + ','
            + DATE + ','
            + TPS + ','
            + PLAYERS_ONLINE + ','
            + CPU_USAGE + ','
            + RAM_USAGE + ','
            + ENTITIES + ','
            + CHUNKS + ','
            + FREE_DISK + ','
            + MSPT_AVERAGE + ','
            + MSPT_95TH_PERCENTILE
            + ") VALUES ("
            + ServerTable.SELECT_SERVER_ID + ','
            + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private TPSTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(SERVER_ID, Sql.INT).notNull()
                .column(DATE, Sql.LONG).notNull()
                .column(TPS, Sql.DOUBLE).notNull()
                .column(PLAYERS_ONLINE, Sql.INT).notNull()
                .column(CPU_USAGE, Sql.DOUBLE).notNull()
                .column(RAM_USAGE, Sql.LONG).notNull()
                .column(ENTITIES, Sql.INT).notNull()
                .column(CHUNKS, Sql.INT).notNull()
                .column(FREE_DISK, Sql.LONG).notNull()
                .column(MSPT_AVERAGE, Sql.DOUBLE) // Nullable
                .column(MSPT_95TH_PERCENTILE, Sql.DOUBLE) // Nullable
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }
}
