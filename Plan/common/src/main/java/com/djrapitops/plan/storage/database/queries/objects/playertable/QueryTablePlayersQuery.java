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
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.sql.tables.GeoInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.transactions.temporary.CreateTemporaryQueryUuidsTableTransaction;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Query for displaying players on /query page players table.
 *
 * @author AuroraLS3
 */
public class QueryTablePlayersQuery implements Query<List<TablePlayer>> {

    private final Collection<UUID> playerUUIDs;
    private final List<ServerUUID> serverUUIDs;
    private final long afterDate;
    private final long beforeDate;
    private final long activeMsThreshold;

    /**
     * Create a new query.
     *
     * @param playerUUIDs       UUIDs of the players in the query
     * @param serverUUIDs       View data for these Server UUIDs
     * @param afterDate         View data after this epoch ms
     * @param beforeDate        View data before this epoch ms
     * @param activeMsThreshold Playtime threshold for Activity Index calculation
     */
    public QueryTablePlayersQuery(Collection<UUID> playerUUIDs, List<ServerUUID> serverUUIDs, long afterDate, long beforeDate, long activeMsThreshold) {
        this.playerUUIDs = playerUUIDs;
        this.serverUUIDs = serverUUIDs;
        this.afterDate = afterDate;
        this.beforeDate = beforeDate;
        this.activeMsThreshold = activeMsThreshold;
    }

    @Override
    public List<TablePlayer> executeQuery(SQLDB db) {
        CreateTemporaryQueryUuidsTableTransaction createTempTables = null;
        try {
            createTempTables = new CreateTemporaryQueryUuidsTableTransaction(playerUUIDs);
            db.executeTransaction(createTempTables).get(); // Wait for transaction to execute
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new DBOpException("Failed to create temporary table for query", e);
        }

        Optional<String> tableName = createTempTables.getTableName();
        if (!tableName.isPresent()) return Collections.emptyList();

        String uuidsInSet = " IN (SELECT uuid FROM " + tableName.get() + ")";

        String selectLatestGeolocations = SELECT +
                "a." + GeoInfoTable.USER_ID + ',' +
                "a." + GeoInfoTable.GEOLOCATION +
                FROM + GeoInfoTable.TABLE_NAME + " a" +
                // Super smart optimization https://stackoverflow.com/a/28090544
                // Join the last_used column, but only if there's a bigger one.
                // That way the biggest a.last_used value will have NULL on the b.last_used column and MAX doesn't need to be used.
                LEFT_JOIN + GeoInfoTable.TABLE_NAME + " b ON a." + GeoInfoTable.USER_ID + "=b." + GeoInfoTable.USER_ID + AND + "a." + GeoInfoTable.LAST_USED + "<b." + GeoInfoTable.LAST_USED +
                WHERE + "b." + GeoInfoTable.LAST_USED + IS_NULL;

        String selectSessionData = SELECT + "s." + SessionsTable.USER_UUID + ',' +
                "MAX(" + SessionsTable.SESSION_END + ") as last_seen," +
                "COUNT(1) as count," +
                "SUM(" + SessionsTable.SESSION_END + '-' + SessionsTable.SESSION_START + '-' + SessionsTable.AFK_TIME + ") as active_playtime" +
                FROM + SessionsTable.TABLE_NAME + " s" +
                WHERE + "s." + SessionsTable.SESSION_START + ">=?" +
                AND + "s." + SessionsTable.SESSION_END + "<=?" +
                AND + "s." + SessionsTable.USER_UUID +
                uuidsInSet +
                (serverUUIDs.isEmpty() ? "" : AND + "s." + SessionsTable.SERVER_UUID + " IN ('" + new TextStringBuilder().appendWithSeparators(serverUUIDs, "','") + "')") +
                GROUP_BY + "s." + SessionsTable.USER_UUID;

        String selectBanned = SELECT + DISTINCT + "ub." + UserInfoTable.USER_UUID +
                FROM + UserInfoTable.TABLE_NAME + " ub" +
                WHERE + UserInfoTable.BANNED + "=?" +
                AND + UserInfoTable.USER_UUID + uuidsInSet +
                (serverUUIDs.isEmpty() ? "" : AND + UserInfoTable.SERVER_UUID + " IN ('" + new TextStringBuilder().appendWithSeparators(serverUUIDs, "','") + "')");

        String selectBaseUsers = SELECT +
                "u." + UsersTable.USER_UUID + ',' +
                "u." + UsersTable.USER_NAME + ',' +
                "u." + UsersTable.REGISTERED + ',' +
                "ban." + UserInfoTable.USER_UUID + " as banned," +
                "geo." + GeoInfoTable.GEOLOCATION + ',' +
                "ses.last_seen," +
                "ses.count," +
                "ses.active_playtime," +
                "act.activity_index" +
                FROM + UsersTable.TABLE_NAME + " u" +
                LEFT_JOIN + '(' + selectBanned + ") ban on ban." + UserInfoTable.USER_UUID + "=u." + UsersTable.USER_UUID +
                LEFT_JOIN + '(' + selectLatestGeolocations + ") geo on geo." + GeoInfoTable.USER_ID + "=u." + UsersTable.ID +
                LEFT_JOIN + '(' + selectSessionData + ") ses on ses." + SessionsTable.USER_UUID + "=u." + UsersTable.USER_UUID +
                LEFT_JOIN + '(' + NetworkActivityIndexQueries.selectActivityIndexSQL() + ") act on u." + SessionsTable.USER_UUID + "=act." + UserInfoTable.USER_UUID +
                WHERE + "u." + UserInfoTable.USER_UUID +
                uuidsInSet +
                ORDER_BY + "ses.last_seen DESC";

        return db.query(new QueryStatement<List<TablePlayer>>(selectBaseUsers, 1000) {
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
                            .activityIndex(new ActivityIndex(set.getDouble("activity_index"), beforeDate));
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