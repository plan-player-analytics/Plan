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
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.patches.PingOptimizationPatch;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plugin.api.TimeAmount;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
    private final String insertStatement;

    public PingTable(SQLDB db) {
        super(TABLE_NAME, db);
        insertStatement = "INSERT INTO " + tableName + " (" +
                Col.UUID + ", " +
                Col.SERVER_UUID + ", " +
                Col.DATE + ", " +
                Col.MIN_PING + ", " +
                Col.MAX_PING + ", " +
                Col.AVG_PING +
                ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(TABLE_NAME)
                .primaryKeyIDColumn(supportsMySQLQueries, Col.ID)
                .column(Col.UUID, Sql.varchar(36)).notNull()
                .column(Col.SERVER_UUID, Sql.varchar(36)).notNull()
                .column(Col.DATE, Sql.LONG).notNull()
                .column(Col.MAX_PING, Sql.INT).notNull()
                .column(Col.MIN_PING, Sql.INT).notNull()
                .column(Col.AVG_PING, Sql.DOUBLE).notNull()
                .primaryKey(supportsMySQLQueries, Col.ID)
                .toString());
    }

    public void clean() {
        String sql = "DELETE FROM " + tableName +
                " WHERE (" + Col.DATE + "<?)" +
                " OR (" + Col.MIN_PING + "<0)";

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
                " WHERE " + Col.UUID + "=?";

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
                                    set.getLong(Col.DATE.get()),
                            UUID.fromString(set.getString(Col.SERVER_UUID.get())),
                                    set.getInt(Col.MIN_PING.get()),
                                    set.getInt(Col.MAX_PING.get()),
                                    set.getDouble(Col.AVG_PING.get())
                            )
                    );
                }

                return pings;
            }
        });
    }

    public Map<UUID, List<Ping>> getAllPings() {
        String sql = "SELECT " +
                Col.DATE + ", " +
                Col.MAX_PING + ", " +
                Col.MIN_PING + ", " +
                Col.AVG_PING + ", " +
                Col.UUID + ", " +
                Col.SERVER_UUID +
                " FROM " + tableName;
        return query(new QueryAllStatement<Map<UUID, List<Ping>>>(sql, 100000) {
            @Override
            public Map<UUID, List<Ping>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Ping>> userPings = new HashMap<>();

                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(Col.UUID.get()));
                    UUID serverUUID = UUID.fromString(set.getString(Col.SERVER_UUID.get()));
                    long date = set.getLong(Col.DATE.get());
                    double avgPing = set.getDouble(Col.AVG_PING.get());
                    int minPing = set.getInt(Col.MIN_PING.get());
                    int maxPing = set.getInt(Col.MAX_PING.get());

                    List<Ping> pings = userPings.getOrDefault(uuid, new ArrayList<>());
                    pings.add(new Ping(date, serverUUID,
                            minPing,
                            maxPing,
                            avgPing));
                    userPings.put(uuid, pings);
                }

                return userPings;
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

    public enum Col implements Column {
        ID("id"),
        UUID(UserUUIDTable.Col.UUID.get()),
        SERVER_UUID("server_uuid"),
        DATE("date"),
        MAX_PING("max_ping"),
        AVG_PING("avg_ping"),
        MIN_PING("min_ping");

        private final String name;

        Col(String name) {
            this.name = name;
        }

        @Override
        public String get() {
            return name;
        }

        @Override
        public String toString() {
            return get();
        }
    }

}
