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
package com.djrapitops.plan.db.sql.tables;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.access.queries.OptionalFetchQueries;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.parsing.Sql;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.api.TimeAmount;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing TPS, Players Online and Performance data.
 * <p>
 * Table Name: plan_tps
 *
 * @author Rsl1122
 */
public class TPSTable extends Table {

    public static final String TABLE_NAME = "plan_tps";

    public static final String SERVER_ID = "server_id";
    public static final String DATE = "date";
    public static final String TPS = "tps";
    public static final String PLAYERS_ONLINE = "players_online";
    public static final String CPU_USAGE = "cpu_usage";
    public static final String RAM_USAGE = "ram_usage";
    public static final String ENTITIES = "entities";
    public static final String CHUNKS = "chunks_loaded";
    public static final String FREE_DISK = "free_disk_space";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + SERVER_ID + ", "
            + DATE + ", "
            + TPS + ", "
            + PLAYERS_ONLINE + ", "
            + CPU_USAGE + ", "
            + RAM_USAGE + ", "
            + ENTITIES + ", "
            + CHUNKS + ", "
            + FREE_DISK
            + ") VALUES ("
            + ServerTable.STATEMENT_SELECT_SERVER_ID + ", "
            + "?, ?, ?, ?, ?, ?, ?, ?)";

    public TPSTable(SQLDB db) {
        super(TABLE_NAME, db);
        serverTable = db.getServerTable();
    }

    private final ServerTable serverTable;

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(SERVER_ID, Sql.INT).notNull()
                .column(DATE, Sql.LONG).notNull()
                .column(TPS, Sql.DOUBLE).notNull()
                .column(PLAYERS_ONLINE, Sql.INT).notNull()
                .column(CPU_USAGE, Sql.DOUBLE).notNull()
                .column(RAM_USAGE, Sql.LONG).notNull()
                .column(ENTITIES, Sql.INT).notNull()
                .column(CHUNKS, Sql.INT).notNull()
                .column(FREE_DISK, Sql.LONG).notNull()
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.SERVER_ID)
                .toString();
    }

    public List<TPS> getTPSData(UUID serverUUID) {
        String sql = Select.all(tableName)
                .where(SERVER_ID + "=" + serverTable.statementSelectServerID)
                .toString();

        return query(new QueryStatement<List<TPS>>(sql, 50000) {
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
        });
    }

    public void insertTPS(TPS tps) {
        execute(new ExecStatement(INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, getServerUUID().toString());
                statement.setLong(2, tps.getDate());
                statement.setDouble(3, tps.getTicksPerSecond());
                statement.setInt(4, tps.getPlayers());
                statement.setDouble(5, tps.getCPUUsage());
                statement.setLong(6, tps.getUsedMemory());
                statement.setDouble(7, tps.getEntityCount());
                statement.setDouble(8, tps.getChunksLoaded());
                statement.setLong(9, tps.getFreeDiskSpace());
            }
        });
    }

    public List<TPS> getNetworkOnlineData() {
        Optional<Server> proxyInfo = db.query(OptionalFetchQueries.fetchProxyServerInformation());
        if (!proxyInfo.isPresent()) {
            return new ArrayList<>();
        }
        UUID bungeeUUID = proxyInfo.get().getUuid();

        String sql = "SELECT " +
                DATE + ", " +
                PLAYERS_ONLINE +
                " FROM " + tableName +
                " WHERE " + SERVER_ID + "=" + serverTable.statementSelectServerID;

        return query(new QueryStatement<List<TPS>>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, bungeeUUID.toString());
            }

            @Override
            public List<TPS> processResults(ResultSet set) throws SQLException {
                List<TPS> tpsList = new ArrayList<>();
                while (set.next()) {

                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(DATE))
                            .playersOnline(set.getInt(PLAYERS_ONLINE))
                            .toTPS();

                    tpsList.add(tps);
                }
                return tpsList;
            }
        });
    }

    public Map<Integer, List<TPS>> getPlayersOnlineForServers(Collection<Server> servers) {
        if (servers.isEmpty()) {
            return new HashMap<>();
        }
        TextStringBuilder sql = new TextStringBuilder("SELECT ");
        sql.append(SERVER_ID).append(", ")
                .append(DATE).append(", ")
                .append(PLAYERS_ONLINE)
                .append(" FROM ").append(tableName)
                .append(" WHERE ")
                .append(DATE).append(">").append(System.currentTimeMillis() - TimeAmount.WEEK.toMillis(2L))
                .append(" AND (");
        sql.appendWithSeparators(servers.stream().map(server -> SERVER_ID + "=" + server.getId()).iterator(), " OR ");
        sql.append(")");

        return query(new QueryAllStatement<Map<Integer, List<TPS>>>(sql.toString(), 10000) {
            @Override
            public Map<Integer, List<TPS>> processResults(ResultSet set) throws SQLException {
                Map<Integer, List<TPS>> map = new HashMap<>();
                while (set.next()) {
                    int serverID = set.getInt(SERVER_ID);
                    int playersOnline = set.getInt(PLAYERS_ONLINE);
                    long date = set.getLong(DATE);

                    List<TPS> tpsList = map.getOrDefault(serverID, new ArrayList<>());

                    TPS tps = TPSBuilder.get().date(date)
                            .playersOnline(playersOnline)
                            .toTPS();
                    tpsList.add(tps);

                    map.put(serverID, tpsList);
                }
                return map;
            }
        });
    }
}
