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

import com.djrapitops.plan.gathering.domain.BaseUser;
import com.djrapitops.plan.gathering.domain.UserInfo;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.RowExtractors;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import org.apache.commons.text.TextStringBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link BaseUser} objects.
 *
 * @author AuroraLS3
 */
public class BaseUserQueries {

    private BaseUserQueries() {
        /* Static method class */
    }

    /**
     * Query database for common user information.
     * <p>
     * Only one {@link BaseUser} per player exists unlike {@link UserInfo} which is available per server.
     *
     * @return Map: Player UUID - BaseUser
     */
    public static Query<Collection<BaseUser>> fetchAllBaseUsers() {
        String sql = Select.all(UsersTable.TABLE_NAME).toString();

        return db -> db.queryList(sql, BaseUserQueries::extractBaseUser);
    }

    public static Query<Map<UUID, BaseUser>> fetchAllBaseUsersByUUID() {
        String sql = Select.all(UsersTable.TABLE_NAME).toString();

        return db -> db.queryMap(sql, (results, map) -> {
            BaseUser baseUser = extractBaseUser(results);
            map.put(baseUser.getUuid(), baseUser);
        }, HashMap::new);
    }

    private static BaseUser extractBaseUser(ResultSet set) throws SQLException {
        UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
        String name = set.getString(UsersTable.USER_NAME);
        long registered = set.getLong(UsersTable.REGISTERED);
        int kicked = set.getInt(UsersTable.TIMES_KICKED);

        BaseUser user = new BaseUser(playerUUID, name, registered, kicked);
        user.setId(set.getInt(UsersTable.ID));
        return user;
    }

    /**
     * Query database for common user information of a player.
     * <p>
     * Only one {@link BaseUser} per player exists unlike {@link UserInfo} which is available per server.
     *
     * @param playerUUID UUID of the Player.
     * @return Optional: BaseUser if found, empty if not.
     */
    public static Query<Optional<BaseUser>> fetchBaseUserOfPlayer(UUID playerUUID) {
        String sql = Select.all(UsersTable.TABLE_NAME).where(UsersTable.USER_UUID + "=?").toString();

        return db -> db.queryOptional(sql, BaseUserQueries::extractBaseUser, playerUUID);
    }

    public static Query<Set<Integer>> userIdsOfRegisteredBetween(long after, long before) {
        String sql = SELECT + DISTINCT + UsersTable.ID +
                FROM + UsersTable.TABLE_NAME +
                WHERE + UsersTable.REGISTERED + ">=?" +
                AND + UsersTable.REGISTERED + "<=?";

        return db -> db.querySet(sql, RowExtractors.getInt(UsersTable.ID), after, before);
    }

    public static Query<Optional<Long>> minimumRegisterDate() {
        String sql = SELECT + min(UsersTable.REGISTERED) + " as min" +
                FROM + UsersTable.TABLE_NAME;
        return db -> db.queryOptional(sql, RowExtractors.getLong("min"));
    }

    // Visible for testing
    public static Query<Optional<Integer>> fetchUserId(UUID playerUUID) {
        String sql = Select.from(UsersTable.TABLE_NAME, UsersTable.ID)
                .where(UsersTable.USER_UUID + "=?")
                .toString();

        return db -> db.queryOptional(sql, RowExtractors.getInt(UsersTable.ID), playerUUID);
    }

    public static Query<Set<UUID>> fetchExistingUUIDs(Set<UUID> outOfPlayerUUIDs) {
        String sql = SELECT + UsersTable.USER_UUID +
                FROM + UsersTable.TABLE_NAME +
                WHERE + UsersTable.USER_UUID + " IN ('" + new TextStringBuilder().appendWithSeparators(outOfPlayerUUIDs, "','").get() + "')";

        return db -> db.querySet(sql, RowExtractors.getUUID(UsersTable.USER_UUID));
    }

    public static Query<List<BaseUser>> fetchBaseUsers(int afterId, int limit) {
        String sql = Select.all(UsersTable.TABLE_NAME)
                .where(UsersTable.ID + ">" + afterId)
                .orderBy(UsersTable.ID)
                .limit(limit)
                .toString();
        return db -> db.queryList(sql, BaseUserQueries::extractBaseUser);
    }
}