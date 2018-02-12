package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class GeoInfoTable extends UserIDTable {

    private static final String columnIP = "ip";
    private static final String columnGeolocation = "geolocation";
    private static final String columnLastUsed = "last_used";
    private String insertStatement;

    public GeoInfoTable(SQLDB db) {
        super("plan_ips", db);
        insertStatement = "INSERT INTO " + tableName + " ("
                + columnUserID + ", "
                + columnIP + ", "
                + columnGeolocation + ", "
                + columnLastUsed
                + ") VALUES ("
                + usersTable.statementSelectID + ", "
                + "?, ?, ?)";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnUserID, Sql.INT).notNull()
                .column(columnIP, Sql.varchar(39)).notNull()
                .column(columnGeolocation, Sql.varchar(50)).notNull()
                .column(columnLastUsed, Sql.LONG).notNull().defaultValue("0")
                .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                .toString()
        );
    }

    public void alterTableV12() {
        if (usingMySQL) {
            executeUnsafe("ALTER TABLE " + tableName + " MODIFY " + columnIP + " VARCHAR(39) NOT NULL");
        }
    }

    public void alterTableV13() {
        addColumns(columnLastUsed + " bigint NOT NULL DEFAULT 0");
    }

    public List<GeoInfo> getGeoInfo(UUID uuid) throws SQLException {
        String sql = "SELECT DISTINCT * FROM " + tableName +
                " WHERE " + columnUserID + "=" + usersTable.statementSelectID;

        return query(new QueryStatement<List<GeoInfo>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<GeoInfo> processResults(ResultSet set) throws SQLException {
                List<GeoInfo> geoInfo = new ArrayList<>();
                while (set.next()) {
                    String ip = set.getString(columnIP);
                    String geolocation = set.getString(columnGeolocation);
                    long lastUsed = set.getLong(columnLastUsed);
                    geoInfo.add(new GeoInfo(ip, geolocation, lastUsed));
                }
                return geoInfo;
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

    private void updateGeoInfo(UUID uuid, GeoInfo info) throws SQLException {
        String sql = "UPDATE " + tableName + " SET "
                + columnLastUsed + "=?" +
                " WHERE " + columnUserID + "=" + usersTable.statementSelectID +
                " AND " + columnIP + "=?" +
                " AND " + columnGeolocation + "=?";

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

    public Optional<String> getGeolocation(String ip) throws SQLException {
        String sql = Select.from(tableName, columnGeolocation)
                .where(columnIP + "=?")
                .toString();

        return query(new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ip);
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(columnGeolocation));
                }
                return Optional.empty();
            }
        });
    }

    public Map<UUID, List<GeoInfo>> getAllGeoInfo() throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
        String sql = "SELECT " +
                columnIP + ", " +
                columnGeolocation + ", " +
                columnLastUsed + ", " +
                usersUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID;

        return query(new QueryAllStatement<Map<UUID, List<GeoInfo>>>(sql, 50000) {
            @Override
            public Map<UUID, List<GeoInfo>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<GeoInfo>> geoLocations = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    List<GeoInfo> userGeoInfo = geoLocations.getOrDefault(uuid, new ArrayList<>());

                    String ip = set.getString(columnIP);
                    String geolocation = set.getString(columnGeolocation);
                    long lastUsed = set.getLong(columnLastUsed);
                    userGeoInfo.add(new GeoInfo(ip, geolocation, lastUsed));

                    geoLocations.put(uuid, userGeoInfo);
                }
                return geoLocations;
            }
        });
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
