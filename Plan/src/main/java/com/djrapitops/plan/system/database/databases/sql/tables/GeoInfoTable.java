package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing common IP and Geolocation data for users.
 * <p>
 * Table Name: plan_ips
 * <p>
 * For contained columns {@see Col}
 *
 * @author Rsl1122
 */
public class GeoInfoTable extends UserIDTable {

    public GeoInfoTable(SQLDB db) {
        super("plan_ips", db);
        insertStatement = "INSERT INTO " + tableName + " ("
                + Col.USER_ID + ", "
                + Col.IP + ", "
                + Col.GEOLOCATION + ", "
                + Col.LAST_USED
                + ") VALUES ("
                + usersTable.statementSelectID + ", "
                + "?, ?, ?)";
    }

    private String insertStatement;

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(Col.USER_ID, Sql.INT).notNull()
                .column(Col.IP, Sql.varchar(39)).notNull()
                .column(Col.GEOLOCATION, Sql.varchar(50)).notNull()
                .column(Col.LAST_USED, Sql.LONG).notNull().defaultValue("0")
                .foreignKey(Col.USER_ID, usersTable.getTableName(), UsersTable.Col.ID)
                .toString()
        );
    }

    public void alterTableV12() {
        if (usingMySQL) {
            executeUnsafe("ALTER TABLE " + tableName + " MODIFY " + Col.IP + " VARCHAR(39) NOT NULL");
        }
    }

    public void alterTableV13() {
        addColumns(Col.LAST_USED + " bigint NOT NULL DEFAULT 0");
    }

    public List<GeoInfo> getGeoInfo(UUID uuid) throws SQLException {
        String sql = "SELECT DISTINCT * FROM " + tableName +
                " WHERE " + Col.USER_ID + "=" + usersTable.statementSelectID;

        return query(new QueryStatement<List<GeoInfo>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<GeoInfo> processResults(ResultSet set) throws SQLException {
                List<GeoInfo> geoInfo = new ArrayList<>();
                while (set.next()) {
                    String ip = set.getString(Col.IP.get());
                    String geolocation = set.getString(Col.GEOLOCATION.get());
                    long lastUsed = set.getLong(Col.LAST_USED.get());
                    geoInfo.add(new GeoInfo(ip, geolocation, lastUsed));
                }
                return geoInfo;
            }
        });
    }

    private void updateGeoInfo(UUID uuid, GeoInfo info) throws SQLException {
        String sql = "UPDATE " + tableName + " SET "
                + Col.LAST_USED + "=?" +
                " WHERE " + Col.USER_ID + "=" + usersTable.statementSelectID +
                " AND " + Col.IP + "=?" +
                " AND " + Col.GEOLOCATION + "=?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, info.getLastUsed());
                statement.setString(2, uuid.toString());
                statement.setString(3, info.getIp());
                statement.setString(4, info.getGeolocation());
            }
        });
    }

    public void saveGeoInfo(UUID uuid, GeoInfo info) throws SQLException {
        List<GeoInfo> geoInfo = getGeoInfo(uuid);
        if (geoInfo.contains(info)) {
            updateGeoInfo(uuid, info);
        }
        insertGeoInfo(uuid, info);
    }

    private void insertGeoInfo(UUID uuid, GeoInfo info) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, info.getIp());
                statement.setString(3, info.getGeolocation());
                statement.setLong(4, info.getLastUsed());
            }
        });
    }

    public Optional<String> getGeolocation(String ip) throws SQLException {
        String sql = Select.from(tableName, Col.GEOLOCATION)
                .where(Col.IP + "=?")
                .toString();

        return query(new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ip);
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(Col.GEOLOCATION.get()));
                }
                return Optional.empty();
            }
        });
    }

    public Map<UUID, List<GeoInfo>> getAllGeoInfo() throws SQLException {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as uuid";
        String sql = "SELECT " +
                Col.IP + ", " +
                Col.GEOLOCATION + ", " +
                Col.LAST_USED + ", " +
                usersUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.USER_ID;

        return query(new QueryAllStatement<Map<UUID, List<GeoInfo>>>(sql, 50000) {
            @Override
            public Map<UUID, List<GeoInfo>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<GeoInfo>> geoLocations = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    List<GeoInfo> userGeoInfo = geoLocations.getOrDefault(uuid, new ArrayList<>());

                    String ip = set.getString(Col.IP.get());
                    String geolocation = set.getString(Col.GEOLOCATION.get());
                    long lastUsed = set.getLong(Col.LAST_USED.get());
                    userGeoInfo.add(new GeoInfo(ip, geolocation, lastUsed));

                    geoLocations.put(uuid, userGeoInfo);
                }
                return geoLocations;
            }
        });
    }

    public List<String> getNetworkGeolocations() throws SQLException {
        String subQuery = "SELECT " +
                Col.USER_ID + ", " +
                "MAX(" + Col.LAST_USED + ") as max" +
                " FROM " + tableName +
                " GROUP BY " + Col.USER_ID;
        String sql = "SELECT " +
                "f." + Col.GEOLOCATION +
                " FROM (" + subQuery + ") as x" +
                " INNER JOIN " + tableName + " AS f ON f." + Col.USER_ID + "=x." + Col.USER_ID +
                " AND f." + Col.LAST_USED + "=x.max";

        return query(new QueryAllStatement<List<String>>(sql) {
            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> geolocations = new ArrayList<>();
                while (set.next()) {
                    geolocations.add(set.getString(Col.GEOLOCATION.get()));
                }
                return geolocations;
            }
        });
    }

    public enum Col implements Column {
        USER_ID(UserIDTable.Col.USER_ID.get()),
        IP("ip"),
        GEOLOCATION("geolocation"),
        LAST_USED("last_used");

        private final String column;

        Col(String column) {
            this.column = column;
        }

        @Override
        public String get() {
            return toString();
        }

        @Override
        public String toString() {
            return column;
        }
    }

    public void insertAllGeoInfo(Map<UUID, List<GeoInfo>> allIPsAndGeolocations) throws SQLException {
        if (Verify.isEmpty(allIPsAndGeolocations)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every User
                for (UUID uuid : allIPsAndGeolocations.keySet()) {
                    // Every GeoInfo
                    for (GeoInfo info : allIPsAndGeolocations.get(uuid)) {
                        String ip = info.getIp();
                        String geoLocation = info.getGeolocation();
                        long lastUsed = info.getLastUsed();

                        statement.setString(1, uuid.toString());
                        statement.setString(2, ip);
                        statement.setString(3, geoLocation);
                        statement.setLong(4, lastUsed);

                        statement.addBatch();
                    }
                }
            }
        });
    }
}
