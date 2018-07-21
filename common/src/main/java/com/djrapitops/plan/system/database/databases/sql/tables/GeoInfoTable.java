package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.database.databases.sql.tables.move.Version18TransferTable;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.comparators.GeoInfoComparator;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
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

    private String insertStatement;

    public GeoInfoTable(SQLDB db) {
        super("plan_ips", db);
        insertStatement = "INSERT INTO " + tableName + " ("
                + Col.USER_ID + ", "
                + Col.IP + ", "
                + Col.IP_HASH + ", "
                + Col.GEOLOCATION + ", "
                + Col.LAST_USED
                + ") VALUES ("
                + usersTable.statementSelectID + ", "
                + "?, ?, ?, ?)";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(Col.USER_ID, Sql.INT).notNull()
                .column(Col.IP, Sql.varchar(39)).notNull()
                .column(Col.GEOLOCATION, Sql.varchar(50)).notNull()
                .column(Col.IP_HASH, Sql.varchar(200))
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

    public void alterTableV17() {
        addColumns(Col.IP_HASH.get() + " varchar(200) DEFAULT ''");
    }

    public void alterTableV18() {
        RunnableFactory.createNew("DB Version 17->18", new AbsRunnable() {
            @Override
            public void run() {
                try {
                    Map<UUID, List<GeoInfo>> allGeoInfo = getAllGeoInfo();

                    String sql = "UPDATE " + tableName + " SET " +
                            Col.IP + "=?, " +
                            Col.IP_HASH + "=? " +
                            "WHERE " + Col.IP + "=?";
                    executeBatch(new ExecStatement(sql) {
                        @Override
                        public void prepare(PreparedStatement statement) throws SQLException {
                            for (List<GeoInfo> geoInfos : allGeoInfo.values()) {
                                for (GeoInfo geoInfo : geoInfos) {
                                    try {
                                        if (geoInfo.getIp().endsWith(".xx.xx")) {
                                            continue;
                                        }
                                        GeoInfo updatedInfo = new GeoInfo(
                                                InetAddress.getByName(geoInfo.getIp()),
                                                geoInfo.getGeolocation(),
                                                geoInfo.getDate()
                                        );
                                        statement.setString(1, updatedInfo.getIp());
                                        statement.setString(2, updatedInfo.getIpHash());
                                        statement.setString(3, geoInfo.getIp());
                                        statement.addBatch();
                                    } catch (UnknownHostException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
                                        if (Settings.DEV_MODE.isTrue()) {
                                            Log.toLog(this.getClass(), e);
                                        }
                                    }
                                }
                            }
                        }
                    });
                    new Version18TransferTable(db).alterTableV18();
                    db.setVersion(18);
                } catch (DBOpException | DBInitException e) {
                    Log.toLog(this.getClass(), e);
                }
            }
        }).runTaskAsynchronously();
    }

    public void clean() {

    }

    public List<GeoInfo> getGeoInfo(UUID uuid) {
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
                    String ipHash = set.getString(Col.IP_HASH.get());
                    long lastUsed = set.getLong(Col.LAST_USED.get());
                    geoInfo.add(new GeoInfo(ip, geolocation, lastUsed, ipHash));
                }
                return geoInfo;
            }
        });
    }

    private void updateGeoInfo(UUID uuid, GeoInfo info) {
        String sql = "UPDATE " + tableName + " SET "
                + Col.LAST_USED + "=?" +
                " WHERE " + Col.USER_ID + "=" + usersTable.statementSelectID +
                " AND " + Col.IP_HASH + "=?" +
                " AND " + Col.GEOLOCATION + "=?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, info.getDate());
                statement.setString(2, uuid.toString());
                statement.setString(3, info.getIpHash());
                statement.setString(4, info.getGeolocation());
            }
        });
    }

    public void saveGeoInfo(UUID uuid, GeoInfo info) {
        List<GeoInfo> geoInfo = getGeoInfo(uuid);
        if (geoInfo.contains(info)) {
            updateGeoInfo(uuid, info);
        } else {
            insertGeoInfo(uuid, info);
        }
    }

    private void insertGeoInfo(UUID uuid, GeoInfo info) {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, info.getIp());
                statement.setString(3, info.getIpHash());
                statement.setString(4, info.getGeolocation());
                statement.setLong(5, info.getDate());
            }
        });
    }

    public Optional<String> getGeolocation(String ip) {
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

    public Map<UUID, List<GeoInfo>> getAllGeoInfo() {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as uuid";
        String sql = "SELECT " +
                Col.IP + ", " +
                Col.GEOLOCATION + ", " +
                Col.LAST_USED + ", " +
                Col.IP_HASH + ", " +
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
                    String ipHash = set.getString(Col.IP_HASH.get());
                    long lastUsed = set.getLong(Col.LAST_USED.get());
                    userGeoInfo.add(new GeoInfo(ip, geolocation, lastUsed, ipHash));

                    geoLocations.put(uuid, userGeoInfo);
                }
                return geoLocations;
            }
        });
    }

    public List<String> getNetworkGeolocations() {
        List<String> geolocations = new ArrayList<>();

        Map<UUID, List<GeoInfo>> geoInfo = getAllGeoInfo();
        for (List<GeoInfo> userGeoInfos : geoInfo.values()) {
            if (userGeoInfos.isEmpty()) {
                continue;
            }
            userGeoInfos.sort(new GeoInfoComparator());
            geolocations.add(userGeoInfos.get(0).getGeolocation());
        }

        return geolocations;
    }

    public void insertAllGeoInfo(Map<UUID, List<GeoInfo>> allIPsAndGeolocations) {
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
                        String ipHash = info.getIpHash();
                        String geoLocation = info.getGeolocation();
                        long lastUsed = info.getDate();

                        statement.setString(1, uuid.toString());
                        statement.setString(2, ip);
                        statement.setString(3, ipHash);
                        statement.setString(4, geoLocation);
                        statement.setLong(5, lastUsed);

                        statement.addBatch();
                    }
                }
            }
        });
    }

    public enum Col implements Column {
        USER_ID(UserIDTable.Col.USER_ID.get()),
        IP("ip"),
        IP_HASH("ip_hash"),
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
}
