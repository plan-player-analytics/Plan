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

import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.patches.NicknameLastSeenPatch;
import com.djrapitops.plan.db.patches.NicknamesOptimizationPatch;
import com.djrapitops.plan.db.patches.Version10Patch;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Table that is in charge of storing nickname data.
 * <p>
 * Table Name: plan_nicknames
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link NicknameLastSeenPatch}
 * {@link NicknamesOptimizationPatch}
 *
 * @author Rsl1122
 */
public class NicknamesTable extends Table {

    public static final String TABLE_NAME = "plan_nicknames";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    public static final String SERVER_UUID = "server_uuid";
    public static final String NICKNAME = "nickname";
    public static final String LAST_USED = "last_used";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            USER_UUID + ", " +
            SERVER_UUID + ", " +
            NICKNAME + ", " +
            LAST_USED +
            ") VALUES (?, ?, ?, ?)";

    public static final String UPDATE_STATEMENT = "UPDATE " + TABLE_NAME + " SET " + LAST_USED + "=?" +
            " WHERE " + NICKNAME + "=?" +
            " AND " + USER_UUID + "=?" +
            " AND " + SERVER_UUID + "=?";

    public NicknamesTable(SQLDB db) {
        super(TABLE_NAME, db);
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(NICKNAME, Sql.varchar(75)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(LAST_USED, Sql.LONG).notNull()
                .toString();
    }

    public List<Nickname> getNicknameInformation(UUID uuid) {
        String sql = "SELECT " +
                NICKNAME + ", " +
                LAST_USED + ", " +
                SERVER_UUID +
                " FROM " + TABLE_NAME +
                " WHERE (" + USER_UUID + "=?)";

        return query(new QueryStatement<List<Nickname>>(sql, 5000) {

            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<Nickname> processResults(ResultSet set) throws SQLException {
                List<Nickname> nicknames = new ArrayList<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(SERVER_UUID));
                    String nickname = set.getString(NICKNAME);
                    nicknames.add(new Nickname(nickname, set.getLong(LAST_USED), serverUUID));
                }
                return nicknames;
            }
        });
    }
}
