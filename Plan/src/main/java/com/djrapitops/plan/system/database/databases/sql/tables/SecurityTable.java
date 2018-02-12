/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Insert;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rsl1122
 */
public class SecurityTable extends Table {

    private static final String columnUser = "username";
    private static final String columnSaltedHash = "salted_pass_hash";
    private static final String columnPermLevel = "permission_level";
    private String insertStatement;

    public SecurityTable(SQLDB db) {
        super("plan_security", db);
        insertStatement = Insert.values(tableName,
                columnUser,
                columnSaltedHash,
                columnPermLevel);
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnUser, Sql.varchar(100)).notNull().unique()
                .column(columnSaltedHash, Sql.varchar(100)).notNull().unique()
                .column(columnPermLevel, Sql.INT).notNull()
                .toString()
        );
    }

    public void removeUser(String user) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE (" + columnUser + "=?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, user);
            }
        });
    }

    public void addNewUser(WebUser info) throws SQLException {
        addNewUser(info.getName(), info.getSaltedPassHash(), info.getPermLevel());
    }

    public void addNewUser(String user, String saltPassHash, int permLevel) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, user);
                statement.setString(2, saltPassHash);
                statement.setInt(3, permLevel);
            }
        });
    }

    public boolean userExists(String user) throws SQLException {
        return getWebUser(user) != null;
    }

    public WebUser getWebUser(String user) throws SQLException {
        String sql = Select.all(tableName).where(columnUser + "=?").toString();

        return query(new QueryStatement<WebUser>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, user);
            }

            @Override
            public WebUser processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    String saltedPassHash = set.getString(columnSaltedHash);
                    int permissionLevel = set.getInt(columnPermLevel);
                    return new WebUser(user, saltedPassHash, permissionLevel);
                }
                return null;
            }
        });
    }

    public List<WebUser> getUsers() throws SQLException {
        String sql = Select.all(tableName).toString();

        return query(new QueryAllStatement<List<WebUser>>(sql, 5000) {
            @Override
            public List<WebUser> processResults(ResultSet set) throws SQLException {
                List<WebUser> list = new ArrayList<>();
                while (set.next()) {
                    String user = set.getString(columnUser);
                    String saltedPassHash = set.getString(columnSaltedHash);
                    int permissionLevel = set.getInt(columnPermLevel);
                    WebUser info = new WebUser(user, saltedPassHash, permissionLevel);
                    list.add(info);
                }
                return list;
            }
        });
    }

    public void addUsers(List<WebUser> users) throws SQLException {
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
