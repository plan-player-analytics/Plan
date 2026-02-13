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
package com.djrapitops.plan.storage.database.queries.objects.playertable;

import com.djrapitops.plan.delivery.domain.TablePlayer;
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Query for displaying players on /query page players table.
 *
 * @author AuroraLS3
 */
public class QueryTablePlayersQuery implements Query<List<TablePlayer>> {

    private final Collection<Integer> userIds;
    private final List<ServerUUID> serverUUIDs;
    private final long afterDate;
    private final long beforeDate;
    private final long activeMsThreshold;

    /**
     * Create a new query.
     *
     * @param userIds           User ids of the players in the query
     * @param serverUUIDs       View data for these Server UUIDs
     * @param afterDate         View data after this epoch ms
     * @param beforeDate        View data before this epoch ms
     * @param activeMsThreshold Playtime threshold for Activity Index calculation
     */
    public QueryTablePlayersQuery(Collection<Integer> userIds, List<ServerUUID> serverUUIDs, long afterDate, long beforeDate, long activeMsThreshold) {
        this.userIds = userIds;
        this.serverUUIDs = serverUUIDs;
        this.afterDate = afterDate;
        this.beforeDate = beforeDate;
        this.activeMsThreshold = activeMsThreshold;
    }

    @Override
    public List<TablePlayer> executeQuery(SQLDB db) {
        String selectServerIds = SELECT + ServerTable.ID +
                FROM + ServerTable.TABLE_NAME +
                WHERE + ServerTable.SERVER_UUID + " IN ('" + new TextStringBuilder().appendWithSeparators(serverUUIDs, "','") + "')";

        String selectLatestGeolocations = SELECT +
                "a." + GeoInfoTable.USER_ID + ',' +
                "a." + GeoInfoTable.GEOLOCATION +
                FROM + GeoInfoTable.TABLE_NAME + " a" +
                // Super smart optimization https://stackoverflow.com/a/28090544
                // Join the last_used column, but only if there's a bigger one.
                // That way the biggest a.last_used value will have NULL on the b.last_used column and MAX doesn't need to be used.
                LEFT_JOIN + GeoInfoTable.TABLE_NAME + " b ON a." + GeoInfoTable.USER_ID + "=b." + GeoInfoTable.USER_ID + AND + "a." + GeoInfoTable.LAST_USED + "<b." + GeoInfoTable.LAST_USED +
                WHERE + "b." + GeoInfoTable.LAST_USED + IS_NULL;

        String userIdsInSet = " IN (" + new TextStringBuilder().appendWithSeparators(userIds, ",") + ')';
        String selectSessionData = SELECT + "s." + SessionsTable.USER_ID + ',' +
                "MAX(" + SessionsTable.SESSION_END + ") as last_seen," +
                "COUNT(1) as count," +
                "SUM(" + db.getSql().least(SessionsTable.SESSION_END + "," + beforeDate) + "-" + db.getSql().greatest(SessionsTable.SESSION_START + "," + afterDate) + "-" + SessionsTable.AFK_TIME + ") as active_playtime" +
                FROM + SessionsTable.TABLE_NAME + " s" +
                (serverUUIDs.isEmpty() ? "" : INNER_JOIN + '(' + selectServerIds + ") sel_servers on sel_servers." + ServerTable.ID + "=s." + SessionsTable.SERVER_ID) +
                WHERE + "s." + SessionsTable.SESSION_END + ">=?" +
                AND + "s." + SessionsTable.SESSION_START + "<=?" +
                AND + "s." + SessionsTable.USER_ID + userIdsInSet +
                GROUP_BY + "s." + SessionsTable.USER_ID;

        String selectBanned = SELECT + DISTINCT + "ub." + UserInfoTable.USER_ID +
                FROM + UserInfoTable.TABLE_NAME + " ub" +
                WHERE + UserInfoTable.BANNED + "=?" +
                AND + "ub." + UserInfoTable.USER_ID + userIdsInSet +
                (serverUUIDs.isEmpty() ? "" : AND + "ub." + UserInfoTable.SERVER_ID + " IN (" + selectServerIds + ")");

        String selectPingData = SELECT + "p." + PingTable.USER_ID + ',' +
                "AVG(p." + PingTable.AVG_PING + ") as " + PingTable.AVG_PING + "," +
                "MAX(p." + PingTable.MAX_PING + ") as " + PingTable.MAX_PING + "," +
                "MIN(p." + PingTable.MIN_PING + ") as " + PingTable.MIN_PING +
                FROM + PingTable.TABLE_NAME + " p" +
                WHERE + "p." + PingTable.USER_ID + userIdsInSet +
                AND + "p." + PingTable.DATE + ">=" + afterDate +
                AND + "p." + PingTable.DATE + "<=" + beforeDate +
                (serverUUIDs.isEmpty() ? "" : AND + "p." + PingTable.SERVER_ID + " IN (" + selectServerIds + ")") +
                GROUP_BY + "p." + PingTable.USER_ID;

        String selectBaseUsers = SELECT +
                "u." + UsersTable.USER_UUID + ',' +
                "u." + UsersTable.USER_NAME + ',' +
                "u." + UsersTable.REGISTERED + ',' +
                "ban." + UserInfoTable.USER_ID + " as banned," +
                "geo." + GeoInfoTable.GEOLOCATION + ',' +
                "ses.last_seen," +
                "ses.count," +
                "ses.active_playtime," +
                "act.activity_index," +
                "pi.min_ping," +
                "pi.max_ping," +
                "pi.avg_ping" +
                FROM + UsersTable.TABLE_NAME + " u" +
                LEFT_JOIN + '(' + selectBanned + ") ban on ban." + UserInfoTable.USER_ID + "=u." + UsersTable.ID +
                LEFT_JOIN + '(' + selectLatestGeolocations + ") geo on geo." + GeoInfoTable.USER_ID + "=u." + UsersTable.ID +
                LEFT_JOIN + '(' + selectSessionData + ") ses on ses." + SessionsTable.USER_ID + "=u." + UsersTable.ID +
                LEFT_JOIN + '(' + NetworkActivityIndexQueries.selectActivityIndexSQL() + ") act on u." + UsersTable.ID + "=act." + UserInfoTable.USER_ID +
                LEFT_JOIN + '(' + selectPingData + ") pi on pi." + PingTable.USER_ID + "=u." + UsersTable.ID +
                WHERE + "u." + UsersTable.ID + userIdsInSet +
                ORDER_BY + "ses.last_seen DESC";

        return db.query(new QueryStatement<>(selectBaseUsers, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
                statement.setLong(2, afterDate);
                statement.setLong(3, beforeDate);
                NetworkActivityIndexQueries.setSelectActivityIndexSQLParameters(statement, 4, activeMsThreshold, beforeDate);
            }

            @Override
            public List<TablePlayer> processResults(ResultSet set) throws SQLException {
                List<TablePlayer> players = new ArrayList<>();
                while (set.next()) {
                    TablePlayer.Builder player = TablePlayer.builder()
                            .uuid(UUID.fromString(set.getString(UsersTable.USER_UUID)))
                            .name(set.getString(UsersTable.USER_NAME))
                            .geolocation(set.getString(GeoInfoTable.GEOLOCATION))
                            .registered(set.getLong(UsersTable.REGISTERED))
                            .lastSeen(set.getLong("last_seen"))
                            .sessionCount(set.getInt("count"))
                            .activePlaytime(set.getLong("active_playtime"))
                            .activityIndex(new ActivityIndex(set.getDouble("activity_index"), beforeDate))
                            .ping(new Ping(0L, null,
                                    set.getInt(PingTable.MIN_PING),
                                    set.getInt(PingTable.MAX_PING),
                                    set.getDouble(PingTable.AVG_PING)));
                    if (set.getString("banned") != null) {
                        player.banned();
                    }
                    players.add(player.build());
                }
                return players;
            }
        });
    }
}