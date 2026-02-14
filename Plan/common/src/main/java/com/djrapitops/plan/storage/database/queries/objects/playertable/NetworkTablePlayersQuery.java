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
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Query for displaying players on /players page.
 *
 * @author AuroraLS3
 */
public class NetworkTablePlayersQuery implements Query<List<TablePlayer>> {

    private final long date;
    private final long activeMsThreshold;
    private final int xMostRecentPlayers;

    public NetworkTablePlayersQuery(long date, long activeMsThreshold, int xMostRecentPlayers) {
        this.date = date;
        this.activeMsThreshold = activeMsThreshold;
        this.xMostRecentPlayers = xMostRecentPlayers;
    }

    @Override
    public List<TablePlayer> executeQuery(SQLDB db) {
        String selectLatestGeolocations = SELECT +
                "a." + GeoInfoTable.USER_ID + ',' +
                "a." + GeoInfoTable.GEOLOCATION +
                FROM + GeoInfoTable.TABLE_NAME + " a" +
                // Super smart optimization https://stackoverflow.com/a/28090544
                // Join the last_used column, but only if there's a bigger one.
                // That way the biggest a.last_used value will have NULL on the b.last_used column and MAX doesn't need to be used.
                LEFT_JOIN + GeoInfoTable.TABLE_NAME + " b ON a." + GeoInfoTable.USER_ID + "=b." + GeoInfoTable.USER_ID + AND + "a." + GeoInfoTable.LAST_USED + "<b." + GeoInfoTable.LAST_USED +
                WHERE + "b." + GeoInfoTable.LAST_USED + IS_NULL;

        String selectSessionData = SELECT + "s." + SessionsTable.USER_ID + ',' +
                "MAX(" + SessionsTable.SESSION_END + ") as last_seen," +
                "COUNT(1) as count," +
                "SUM(" + SessionsTable.SESSION_END + '-' + SessionsTable.SESSION_START + '-' + SessionsTable.AFK_TIME + ") as active_playtime" +
                FROM + SessionsTable.TABLE_NAME + " s" +
                GROUP_BY + "s." + SessionsTable.USER_ID;

        String selectPingData = SELECT +
                "p." + PingTable.USER_ID + ',' +
                "AVG(p." + PingTable.AVG_PING + ") as " + PingTable.AVG_PING + "," +
                "MAX(p." + PingTable.MAX_PING + ") as " + PingTable.MAX_PING + "," +
                "MIN(p." + PingTable.MIN_PING + ") as " + PingTable.MIN_PING +
                FROM + PingTable.TABLE_NAME + " p" +
                GROUP_BY + "p." + PingTable.USER_ID;

        String selectBanned = SELECT + DISTINCT + "ub." + UserInfoTable.USER_ID +
                FROM + UserInfoTable.TABLE_NAME + " ub" +
                WHERE + UserInfoTable.BANNED + "=?";

        String selectNicknames = SELECT +
                "un." + UsersTable.ID + ',' +
                "GROUP_CONCAT(DISTINCT " + "n." + NicknamesTable.NICKNAME + ",',') as nicknames" +
                FROM + NicknamesTable.TABLE_NAME + " n" +
                INNER_JOIN + UsersTable.TABLE_NAME + " un ON n." + NicknamesTable.USER_UUID + "=un." + UsersTable.USER_UUID +
                GROUP_BY + "un." + UsersTable.ID;

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
                "pi.avg_ping," +
                "ni.nicknames" +
                FROM + UsersTable.TABLE_NAME + " u" +
                LEFT_JOIN + '(' + selectBanned + ") ban on ban." + UserInfoTable.USER_ID + "=u." + UsersTable.ID +
                LEFT_JOIN + '(' + selectLatestGeolocations + ") geo on geo." + GeoInfoTable.USER_ID + "=u." + UsersTable.ID +
                LEFT_JOIN + '(' + selectSessionData + ") ses on ses." + SessionsTable.USER_ID + "=u." + UsersTable.ID +
                LEFT_JOIN + '(' + NetworkActivityIndexQueries.selectActivityIndexSQL() + ") act on u." + UsersTable.ID + "=act." + UserInfoTable.USER_ID +
                LEFT_JOIN + '(' + selectPingData + ") pi on pi." + PingTable.USER_ID + "=u." + UsersTable.ID +
                LEFT_JOIN + '(' + selectNicknames + ") ni on ni." + UsersTable.ID + "=u." + UsersTable.ID +
                ORDER_BY + "ses.last_seen DESC LIMIT ?";

        return db.query(new QueryStatement<>(selectBaseUsers, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
                NetworkActivityIndexQueries.setSelectActivityIndexSQLParameters(statement, 2, activeMsThreshold, date);
                statement.setInt(10, xMostRecentPlayers);
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
                            .activityIndex(new ActivityIndex(set.getDouble("activity_index"), date))
                            .ping(new Ping(0L, null,
                                    set.getInt(PingTable.MIN_PING),
                                    set.getInt(PingTable.MAX_PING),
                                    set.getDouble(PingTable.AVG_PING)))
                            .nicknames(set.getString("nicknames"));
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