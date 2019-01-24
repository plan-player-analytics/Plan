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
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.patches.*;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.parsing.Sql;
import com.djrapitops.plan.db.sql.parsing.TableSqlParser;
import com.djrapitops.plan.db.sql.queries.LargeFetchQueries;
import com.djrapitops.plan.utilities.comparators.GeoInfoComparator;
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
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link GeoInfoLastUsedPatch}
 * {@link IPAnonPatch}
 * {@link IPHashPatch}
 * {@link GeoInfoOptimizationPatch}
 *
 * @author Rsl1122
 */
public class GeoInfoTable extends UserUUIDTable {

    public static final String TABLE_NAME = "plan_ips";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    public static final String IP = "ip";
    public static final String IP_HASH = "ip_hash";
    public static final String GEOLOCATION = "geolocation";
    public static final String LAST_USED = "last_used";

    public GeoInfoTable(SQLDB db) {
        super(TABLE_NAME, db);
        insertStatement = "INSERT INTO " + tableName + " ("
                + USER_UUID + ", "
                + IP + ", "
                + IP_HASH + ", "
                + GEOLOCATION + ", "
                + LAST_USED
                + ") VALUES (?, ?, ?, ?, ?)";
    }

    private String insertStatement;

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(IP, Sql.varchar(39)).notNull()
                .column(GEOLOCATION, Sql.varchar(50)).notNull()
                .column(IP_HASH, Sql.varchar(200))
                .column(LAST_USED, Sql.LONG).notNull().defaultValue("0")
                .toString();
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(supportsMySQLQueries, ID)
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(IP, Sql.varchar(39)).notNull()
                .column(GEOLOCATION, Sql.varchar(50)).notNull()
                .column(IP_HASH, Sql.varchar(200))
                .column(LAST_USED, Sql.LONG).notNull().defaultValue("0")
                .primaryKey(supportsMySQLQueries, ID)
                .toString()
        );
    }

    public List<GeoInfo> getGeoInfo(UUID uuid) {
        String sql = "SELECT DISTINCT * FROM " + tableName +
                " WHERE " + USER_UUID + "=?";

        return query(new QueryStatement<List<GeoInfo>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<GeoInfo> processResults(ResultSet set) throws SQLException {
                List<GeoInfo> geoInfo = new ArrayList<>();
                while (set.next()) {
                    String ip = set.getString(IP);
                    String geolocation = set.getString(GEOLOCATION);
                    String ipHash = set.getString(IP_HASH);
                    long lastUsed = set.getLong(LAST_USED);
                    geoInfo.add(new GeoInfo(ip, geolocation, lastUsed, ipHash));
                }
                return geoInfo;
            }
        });
    }

    private void updateGeoInfo(UUID uuid, GeoInfo info) {
        String sql = "UPDATE " + tableName + " SET "
                + LAST_USED + "=?" +
                " WHERE " + USER_UUID + "=?" +
                " AND " + IP_HASH + "=?" +
                " AND " + GEOLOCATION + "=?";

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
        String sql = Select.from(tableName, GEOLOCATION)
                .where(IP + "=?")
                .toString();

        return query(new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ip);
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(GEOLOCATION));
                }
                return Optional.empty();
            }
        });
    }

    public List<String> getNetworkGeolocations() {
        List<String> geolocations = new ArrayList<>();

        Map<UUID, List<GeoInfo>> geoInfo = db.query(LargeFetchQueries.fetchAllGeoInfoData());
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
}
