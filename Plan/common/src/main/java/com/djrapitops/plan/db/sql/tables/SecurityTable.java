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

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Insert;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.parsing.Sql;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Table that is in charge of storing WebUser data.
 * <p>
 * Table Name: plan_security
 *
 * @author Rsl1122
 * @see WebUser
 */
public class SecurityTable extends Table {

    public static final String TABLE_NAME = "plan_security";

    public static final String USERNAME = "username";
    public static final String SALT_PASSWORD_HASH = "salted_pass_hash";
    public static final String PERMISSION_LEVEL = "permission_level";

    public SecurityTable(SQLDB db) {
        super(TABLE_NAME, db);
        insertStatement = Insert.values(tableName,
                USERNAME,
                SALT_PASSWORD_HASH,
                PERMISSION_LEVEL);
    }

    private String insertStatement;

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(USERNAME, Sql.varchar(100)).notNull().unique()
                .column(SALT_PASSWORD_HASH, Sql.varchar(100)).notNull().unique()
                .column(PERMISSION_LEVEL, Sql.INT).notNull()
                .toString();
    }

    public void removeUser(String user) {
        String sql = "DELETE FROM " + tableName + " WHERE (" + USERNAME + "=?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, user);
            }
        });
    }

    public WebUser getWebUser(String user) {
        String sql = Select.all(tableName).where(USERNAME + "=?").toString();

        return query(new QueryStatement<WebUser>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, user);
            }

            @Override
            public WebUser processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    String saltedPassHash = set.getString(SALT_PASSWORD_HASH);
                    int permissionLevel = set.getInt(PERMISSION_LEVEL);
                    return new WebUser(user, saltedPassHash, permissionLevel);
                }
                return null;
            }
        });
    }

    public void addNewUser(WebUser info) {
        addNewUser(info.getName(), info.getSaltedPassHash(), info.getPermLevel());
    }

    public void addNewUser(String user, String saltPassHash, int permLevel) {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, user);
                statement.setString(2, saltPassHash);
                statement.setInt(3, permLevel);
            }
        });
    }

    public boolean userExists(String user) {
        return getWebUser(user) != null;
    }

    public void addUsers(List<WebUser> users) {
        if (Verify.isEmpty(users)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (WebUser user : users) {
                    String userName = user.getName();
                    String pass = user.getSaltedPassHash();
                    int permLvl = user.getPermLevel();

                    statement.setString(1, userName);
                    statement.setString(2, pass);
                    statement.setInt(3, permLvl);
                    statement.addBatch();
                }
            }
        });
    }
}
