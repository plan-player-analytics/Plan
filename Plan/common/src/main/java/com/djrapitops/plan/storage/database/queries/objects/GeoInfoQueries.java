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
package com.djrapitops.plan.storage.database.queries.objects;

import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.RowExtractors;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.GeoInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Lists;
import org.apache.commons.text.TextStringBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link GeoInfo} objects.
 *
 * @author AuroraLS3
 */
public class GeoInfoQueries {

    private GeoInfoQueries() {
        /* Static method class */
    }

    /**
     * Query database for all GeoInfo data.
     *
     * @return Map: Player UUID - List of GeoInfo
     */
    public static Query<Map<UUID, List<GeoInfo>>> fetchAllGeoInformation() {
        String sql = SELECT +
                GeoInfoTable.GEOLOCATION + ',' +
                GeoInfoTable.LAST_USED + ',' +
                UsersTable.USER_UUID +
                FROM + GeoInfoTable.TABLE_NAME + " g" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on g.user_id=u.id";

        return new QueryAllStatement<>(sql, 10000) {
            @Override
            public Map<UUID, List<GeoInfo>> processResults(ResultSet set) throws SQLException {
                return extractGeoInformation(set);
            }
        };
    }

    private static Map<UUID, List<GeoInfo>> extractGeoInformation(ResultSet set) throws SQLException {
        Map<UUID, List<GeoInfo>> geoInformation = new HashMap<>();
        while (set.next()) {
            UUID uuid = UUID.fromString(set.getString(UsersTable.USER_UUID));

            List<GeoInfo> userGeoInfo = geoInformation.computeIfAbsent(uuid, Lists::create);
            GeoInfo geoInfo = new GeoInfo(set.getString(GeoInfoTable.GEOLOCATION), set.getLong(GeoInfoTable.LAST_USED));
            userGeoInfo.add(geoInfo);
        }
        return geoInformation;
    }

    /**
     * Query Player's GeoInfo by player's UUID.
     *
     * @param playerUUID UUID of the player.
     * @return List of {@link GeoInfo}, empty if none are found.
     */
    public static Query<List<GeoInfo>> fetchPlayerGeoInformation(UUID playerUUID) {
        String sql = SELECT +
                GeoInfoTable.GEOLOCATION +
                ",MAX(" + GeoInfoTable.LAST_USED + ") as " + GeoInfoTable.LAST_USED +
                FROM + GeoInfoTable.TABLE_NAME +
                WHERE + GeoInfoTable.USER_ID + "=" + UsersTable.SELECT_USER_ID +
                GROUP_BY + GeoInfoTable.GEOLOCATION;

        return db -> db.queryList(sql, GeoInfoQueries::extractGeoInfo, playerUUID);
    }

    private static GeoInfo extractGeoInfo(ResultSet set) throws SQLException {
        String geolocation = set.getString(GeoInfoTable.GEOLOCATION);
        long lastUsed = set.getLong(GeoInfoTable.LAST_USED);
        return new GeoInfo(geolocation, lastUsed);
    }

    public static Query<Map<String, Integer>> networkGeolocationCounts() {
        String sql = SELECT +
                "a." + GeoInfoTable.GEOLOCATION + ", " +
                "COUNT(1) as c" +
                FROM + GeoInfoTable.TABLE_NAME + " a" +
                // Super smart optimization https://stackoverflow.com/a/28090544
                // Join the last_used column, but only if there's a bigger one.
                // That way the biggest a.last_used value will have NULL on the b.last_used column and MAX doesn't need to be used.
                LEFT_JOIN + GeoInfoTable.TABLE_NAME + " b ON a." + GeoInfoTable.USER_ID + "=b." + GeoInfoTable.USER_ID + AND + "a." + GeoInfoTable.LAST_USED + "<b." + GeoInfoTable.LAST_USED +
                WHERE + "b." + GeoInfoTable.LAST_USED + IS_NULL +
                GROUP_BY + "a." + GeoInfoTable.GEOLOCATION;

        return db -> db.queryMap(sql, GeoInfoQueries::extractGeolocationCounts);
    }

    private static void extractGeolocationCounts(ResultSet set, Map<String, Integer> geolocationCounts) throws SQLException {
        geolocationCounts.put(
                set.getString(GeoInfoTable.GEOLOCATION),
                set.getInt("c")
        );
    }

    public static Query<Map<String, Integer>> networkGeolocationCounts(Collection<Integer> userIds) {
        String sql = SELECT +
                "a." + GeoInfoTable.GEOLOCATION + ", " +
                "COUNT(1) as c" +
                FROM + GeoInfoTable.TABLE_NAME + " a" +
                // Super smart optimization https://stackoverflow.com/a/28090544
                // Join the last_used column, but only if there's a bigger one.
                // That way the biggest a.last_used value will have NULL on the b.last_used column and MAX doesn't need to be used.
                LEFT_JOIN + GeoInfoTable.TABLE_NAME + " b ON a." + GeoInfoTable.USER_ID + "=b." + GeoInfoTable.USER_ID + AND + "a." + GeoInfoTable.LAST_USED + "<b." + GeoInfoTable.LAST_USED +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on a." + GeoInfoTable.USER_ID + "=u." + UsersTable.ID +
                WHERE + "b." + GeoInfoTable.LAST_USED + IS_NULL +
                AND + "u." + UsersTable.ID + " IN (" +
                new TextStringBuilder().appendWithSeparators(userIds, ",").build() + ")" +
                GROUP_BY + "a." + GeoInfoTable.GEOLOCATION;

        return db -> db.queryMap(sql, GeoInfoQueries::extractGeolocationCounts);
    }

    public static Query<Map<String, Integer>> serverGeolocationCounts(ServerUUID serverUUID) {
        String sql = SELECT +
                "a." + GeoInfoTable.GEOLOCATION + ", " +
                "COUNT(1) as c" +
                FROM + GeoInfoTable.TABLE_NAME + " a" +
                // Super smart optimization https://stackoverflow.com/a/28090544
                // Join the last_used column, but only if there's a bigger one.
                // That way the biggest a.last_used value will have NULL on the b.last_used column and MAX doesn't need to be used.
                LEFT_JOIN + GeoInfoTable.TABLE_NAME + " b ON a." + GeoInfoTable.USER_ID + "=b." + GeoInfoTable.USER_ID + AND + "a." + GeoInfoTable.LAST_USED + "<b." + GeoInfoTable.LAST_USED +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.ID + "=a." + GeoInfoTable.USER_ID +
                INNER_JOIN + UserInfoTable.TABLE_NAME + " ui on ui." + UserInfoTable.USER_ID + "=u." + UsersTable.ID +
                WHERE + "b." + GeoInfoTable.LAST_USED + IS_NULL +
                AND + "ui." + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                GROUP_BY + "a." + GeoInfoTable.GEOLOCATION;

        return db -> db.queryMap(sql, GeoInfoQueries::extractGeolocationCounts, serverUUID);
    }

    public static Query<List<String>> uniqueGeolocations() {
        String sql = SELECT + DISTINCT + GeoInfoTable.GEOLOCATION + FROM + GeoInfoTable.TABLE_NAME +
                ORDER_BY + GeoInfoTable.GEOLOCATION + " ASC";

        return db -> db.queryList(sql, RowExtractors.getString(GeoInfoTable.GEOLOCATION));
    }

    public static Query<Set<Integer>> userIdsOfPlayersWithGeolocations(@Untrusted List<String> selected) {
        String sql = SELECT + "u." + UsersTable.ID +
                FROM + GeoInfoTable.TABLE_NAME + " g" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u.id=g." + GeoInfoTable.USER_ID +
                WHERE + "LOWER(" + GeoInfoTable.GEOLOCATION + ")" +
                " IN (" + Sql.nParameters(selected.size()) + ")";
        return db -> db.querySet(sql, RowExtractors.getInt(UsersTable.ID), selected.stream().map(String::toLowerCase).collect(Collectors.toList()));
    }
}