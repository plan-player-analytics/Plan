package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.api.TimeAmount;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PingTable extends UserIDTable {

    public static final String TABLE_NAME = "plan_ping";
    private final String insertStatement;
    private final ServerTable serverTable;

    public PingTable(SQLDB db) {
        super(TABLE_NAME, db);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " (" +
                Col.USER_ID + ", " +
                Col.SERVER_ID + ", " +
                Col.DATE + ", " +
                Col.MIN_PING + ", " +
                Col.MAX_PING + ", " +
                Col.AVG_PING +
                ") VALUES (" +
                usersTable.statementSelectID + ", " +
                serverTable.statementSelectServerID + ", ?, ?, ?, ?)";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(TABLE_NAME)
                .primaryKeyIDColumn(usingMySQL, Col.ID)
                .column(Col.USER_ID, Sql.INT).notNull()
                .column(Col.SERVER_ID, Sql.INT).notNull()
                .column(Col.DATE, Sql.LONG).notNull()
                .column(Col.MAX_PING, Sql.INT).notNull()
                .column(Col.MIN_PING, Sql.INT).notNull()
                .column(Col.AVG_PING, Sql.DOUBLE).notNull()
                .primaryKey(usingMySQL, Col.ID)
                .foreignKey(Col.USER_ID, usersTable.getTableName(), UsersTable.Col.ID)
                .foreignKey(Col.SERVER_ID, ServerTable.TABLE_NAME, ServerTable.Col.SERVER_ID)
                .toString());
    }

    public void clean() {
        String sql = "DELETE FROM " + tableName +
                " WHERE (" + Col.DATE + "<?)" +
                " OR (" + Col.MIN_PING + "<0)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                long twoWeeks = TimeAmount.WEEK.ms() * 2L;
                statement.setLong(1, System.currentTimeMillis() - twoWeeks);
            }
        });
    }

    public void insertPing(UUID uuid, Ping ping) {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, ServerInfo.getServerUUID().toString());
                statement.setLong(3, ping.getDate());
                statement.setInt(4, ping.getMin());
                statement.setInt(5, ping.getMax());
                statement.setDouble(6, ping.getAverage());
            }
        });
    }

    public List<Ping> getPing(UUID uuid) {
        Map<Integer, UUID> serverUUIDs = serverTable.getServerUUIDsByID();
        String sql = "SELECT * FROM " + tableName +
                " WHERE " + Col.USER_ID + "=" + usersTable.statementSelectID;

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
                                    serverUUIDs.get(set.getInt(Col.SERVER_ID.get())),
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
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as uuid";
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                Col.DATE + ", " +
                Col.MAX_PING + ", " +
                Col.MIN_PING + ", " +
                Col.AVG_PING + ", " +
                usersUUIDColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + UserInfoTable.Col.USER_ID +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + UserInfoTable.Col.SERVER_ID;
        return query(new QueryAllStatement<Map<UUID, List<Ping>>>(sql, 100000) {
            @Override
            public Map<UUID, List<Ping>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Ping>> userPings = new HashMap<>();

                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
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
        USER_ID(UserIDTable.Col.USER_ID.get()),
        SERVER_ID("server_id"),
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
