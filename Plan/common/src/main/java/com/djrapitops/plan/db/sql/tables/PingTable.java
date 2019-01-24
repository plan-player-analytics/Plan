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

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.patches.PingOptimizationPatch;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Sql;
import com.djrapitops.plugin.api.TimeAmount;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Table that represents plan_ping in the database.
 * <p>
 * Patches related to this table:
 * {@link PingOptimizationPatch}
 *
 * @author Rsl1122
 */
public class PingTable extends UserUUIDTable {

    public static final String TABLE_NAME = "plan_ping";

    public static final String ID = "id";
    public static final String USER_UUID = UserUUIDTable.Col.UUID.get();
    public static final String SERVER_UUID = "server_uuid";
    public static final String DATE = "date";
    public static final String MAX_PING = "max_ping";
    public static final String AVG_PING = "avg_ping";
    public static final String MIN_PING = "min_ping";

    private final String insertStatement;

    public PingTable(SQLDB db) {
        super(TABLE_NAME, db);
        insertStatement = "INSERT INTO " + tableName + " (" +
                USER_UUID + ", " +
                SERVER_UUID + ", " +
                DATE + ", " +
                MIN_PING + ", " +
                MAX_PING + ", " +
                AVG_PING +
                ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(DATE, Sql.LONG).notNull()
                .column(MAX_PING, Sql.INT).notNull()
                .column(MIN_PING, Sql.INT).notNull()
                .column(AVG_PING, Sql.DOUBLE).notNull()
                .toString();
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(createTableSQL(db.getType()));
    }

    public void clean() {
        String sql = "DELETE FROM " + tableName +
                " WHERE (" + DATE + "<?)" +
                " OR (" + MIN_PING + "<0)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                long twoWeeks = TimeAmount.WEEK.toMillis(2L);
                statement.setLong(1, System.currentTimeMillis() - twoWeeks);
            }
        });
    }

    public void insertPing(UUID uuid, Ping ping) {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, getServerUUID().toString());
                statement.setLong(3, ping.getDate());
                statement.setInt(4, ping.getMin());
                statement.setInt(5, ping.getMax());
                statement.setDouble(6, ping.getAverage());
            }
        });
    }

    public List<Ping> getPing(UUID uuid) {
        String sql = "SELECT * FROM " + tableName +
                " WHERE " + USER_UUID + "=?";

        return query(new QueryStatement<List<Ping>>(sql, 10000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<Ping> processResults(ResultSet set) throws SQLException {
                List<Ping> pings = new ArrayList<>();

                while (set.next()) {
                    pings.add(new Ping(
                            set.getLong(DATE),
                            UUID.fromString(set.getString(SERVER_UUID)),
                            set.getInt(MIN_PING),
                            set.getInt(MAX_PING),
                            set.getDouble(AVG_PING)
                            )
                    );
                }

                return pings;
            }
        });
    }

    public void insertAllPings(Map<UUID, List<Ping>> userPings) {
        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<UUID, List<Ping>> entry : userPings.entrySet()) {
                    UUID uuid = entry.getKey();
                    List<Ping> pings = entry.getValue();
                    for (Ping ping : pings) {
                        UUID serverUUID = ping.getServerUUID();
                        long date = ping.getDate();
                        int minPing = ping.getMin();
                        int maxPing = ping.getMax();
                        double avgPing = ping.getAverage();

                        statement.setString(1, uuid.toString());
                        statement.setString(2, serverUUID.toString());
                        statement.setLong(3, date);
                        statement.setInt(4, minPing);
                        statement.setInt(5, maxPing);
                        statement.setDouble(6, avgPing);
                        statement.addBatch();
                    }
                }
            }
        });
    }
}
