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
import com.djrapitops.plan.storage.database.queries.objects.lookup.ServerIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Insert;
import com.djrapitops.plan.storage.database.sql.building.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public static class Row implements ServerIdentifiable {
        public static String INSERT_STATEMENT = Insert.values(TABLE_NAME, SERVER_ID, DATE, TPS, PLAYERS_ONLINE,
                CPU_USAGE, RAM_USAGE, ENTITIES, CHUNKS, FREE_DISK, MSPT_AVERAGE, MSPT_95TH_PERCENTILE);

        public int id;
        public int serverId;
        public long date;
        public double tps;
        public int playersOnline;
        public double cpuUsage;
        public long ramUsage;
        public int entities;
        public int chunksLoaded;
        public long freeDiskSpace;
        public Double msptAverage;
        public Double mspt95thPercentile;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.serverId = set.getInt(SERVER_ID);
            row.date = set.getLong(DATE);
            row.tps = set.getDouble(TPS);
            row.playersOnline = set.getInt(PLAYERS_ONLINE);
            row.cpuUsage = set.getDouble(CPU_USAGE);
            row.ramUsage = set.getLong(RAM_USAGE);
            row.entities = set.getInt(ENTITIES);
            row.chunksLoaded = set.getInt(CHUNKS);
            row.freeDiskSpace = set.getLong(FREE_DISK);
            row.msptAverage = Sql.getDoubleOrNull(set, MSPT_AVERAGE);
            row.mspt95thPercentile = Sql.getDoubleOrNull(set, MSPT_95TH_PERCENTILE);
            return row;
        }

        public static void insert(PreparedStatement statement, Row row) throws SQLException {
            statement.setInt(1, row.serverId);
            statement.setLong(2, row.date);
            statement.setDouble(3, row.tps);
            statement.setInt(4, row.playersOnline);
            statement.setDouble(5, row.cpuUsage);
            statement.setLong(6, row.ramUsage);
            statement.setInt(7, row.entities);
            statement.setInt(8, row.chunksLoaded);
            statement.setLong(9, row.freeDiskSpace);
            Sql.setDoubleOrNull(statement, 10, row.msptAverage);
            Sql.setDoubleOrNull(statement, 11, row.mspt95thPercentile);
        }

        @Override
        public int getServerId() {
            return serverId;
        }

        @Override
        public void setServerId(int serverId) {
            this.serverId = serverId;
        }
    }
}
