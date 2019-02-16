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

import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Insert;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.parsing.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Table that is in charge of storing common player data for all servers.
 * <p>
 * Table Name: plan_users
 *
 * @author Rsl1122
 */
public class UsersTable extends Table {

    public static final String TABLE_NAME = "plan_users";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    public static final String REGISTERED = "registered";
    public static final String USER_NAME = "name";
    public static final String TIMES_KICKED = "times_kicked";

    public static final String INSERT_STATEMENT = Insert.values(TABLE_NAME, USER_UUID, USER_NAME, REGISTERED, TIMES_KICKED);

    public UsersTable(SQLDB db) {
        super(TABLE_NAME, db);
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull().unique()
                .column(REGISTERED, Sql.LONG).notNull()
                .column(USER_NAME, Sql.varchar(16)).notNull()
                .column(TIMES_KICKED, Sql.INT).notNull().defaultValue("0")
                .toString();
    }

    public void kicked(UUID uuid) {
        String sql = "UPDATE " + tableName + " SET "
                + TIMES_KICKED + "=" + TIMES_KICKED + "+ 1" +
                " WHERE " + USER_UUID + "=?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }
        });
    }

    public String getPlayerName(UUID uuid) {
        String sql = Select.from(tableName, USER_NAME).where(USER_UUID + "=?").toString();

        return query(new QueryStatement<String>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public String processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getString(USER_NAME);
                }
                return null;
            }
        });
    }

    /**
     * Gets the names of the players which names or nicknames match {@code name}.
     *
     * @param name the name / nickname.
     * @return a list of distinct names.
     */
    public List<String> getMatchingNames(String name) {
        String searchString = "%" + name + "%";
        String sql = "SELECT DISTINCT " + USER_NAME + " FROM " + tableName +
                " WHERE LOWER(" + USER_NAME + ") LIKE LOWER(?)" +
                " UNION SELECT DISTINCT " + USER_NAME + " FROM " + tableName +
                " INNER JOIN " + NicknamesTable.TABLE_NAME + " on " + tableName + "." + USER_UUID + "=" + NicknamesTable.TABLE_NAME + "." + NicknamesTable.USER_UUID +
                " WHERE LOWER(" + NicknamesTable.NICKNAME + ") LIKE LOWER(?)";

        return query(new QueryStatement<List<String>>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, searchString);
                statement.setString(2, searchString);
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> matchingNames = new ArrayList<>();
                while (set.next()) {
                    String match = set.getString("name");
                    if (!matchingNames.contains(match)) {
                        matchingNames.add(match);
                    }
                }
                return matchingNames;
            }
        });
    }
}
