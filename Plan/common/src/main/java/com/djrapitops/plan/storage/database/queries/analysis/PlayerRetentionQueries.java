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
package com.djrapitops.plan.storage.database.queries.analysis;

import com.djrapitops.plan.delivery.domain.RetentionData;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.util.List;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Contains queries related to player retention data.
 *
 * @author AuroraLS3
 */
public class PlayerRetentionQueries {

    private PlayerRetentionQueries() {
        /* Static method class */
    }

    public static Query<List<RetentionData>> fetchRetentionData(ServerUUID serverUUID) {
        String sql = SELECT +
                UsersTable.USER_UUID + ',' +
                "ui." + UserInfoTable.REGISTERED + ',' +
                "MAX(" + SessionsTable.SESSION_END + ") as last_seen," +
                "SUM(" + SessionsTable.SESSION_END + "-" + SessionsTable.SESSION_START + ") as playtime" +
                FROM + UsersTable.TABLE_NAME + " u" +
                INNER_JOIN + UserInfoTable.TABLE_NAME + " ui ON ui." + UserInfoTable.USER_ID + "=u." + UsersTable.ID +
                INNER_JOIN + SessionsTable.TABLE_NAME + " s ON s." + SessionsTable.USER_ID + "=u." + UsersTable.ID +
                AND + "s." + SessionsTable.SERVER_ID + "=ui." + UserInfoTable.SERVER_ID +
                WHERE + "s." + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                GROUP_BY + UsersTable.USER_UUID + ",ui." + UserInfoTable.REGISTERED;

        return db -> db.queryList(sql, set -> {
            UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
            long registerDate = set.getLong(UserInfoTable.REGISTERED);
            long lastSeenDate = set.getLong("last_seen");
            long playtime = set.getLong("playtime");
            return new RetentionData(playerUUID, registerDate, lastSeenDate, playtime);
        }, serverUUID);
    }

    public static Query<List<RetentionData>> fetchRetentionData() {
        String sql = SELECT +
                UsersTable.USER_UUID + ',' +
                UsersTable.REGISTERED + ',' +
                "MAX(" + SessionsTable.SESSION_END + ") as last_seen," +
                "SUM(" + SessionsTable.SESSION_END + "-" + SessionsTable.SESSION_START + ") as playtime" +
                FROM + UsersTable.TABLE_NAME + " u" +
                INNER_JOIN + SessionsTable.TABLE_NAME + " s ON s." + SessionsTable.USER_ID + "=u." + UsersTable.ID +
                GROUP_BY + UsersTable.USER_UUID + ',' + UserInfoTable.REGISTERED;

        return db -> db.queryList(sql, set -> {
            UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
            long registerDate = set.getLong(UsersTable.REGISTERED);
            long lastSeenDate = set.getLong("last_seen");
            long playtime = set.getLong("playtime");
            return new RetentionData(playerUUID, registerDate, lastSeenDate, playtime);
        });
    }
}
