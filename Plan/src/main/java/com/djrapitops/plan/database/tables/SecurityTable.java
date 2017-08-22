/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rsl1122
 */
public class SecurityTable extends Table {

    private final String columnUser;
    private final String columnSaltedHash;
    private final String columnPermLevel;

    public SecurityTable(SQLDB db, boolean usingMySQL) {
        super("plan_security", db, usingMySQL);
        columnUser = "username";
        columnSaltedHash = "salted_pass_hash";
        columnPermLevel = "permission_level";
    }

    @Override
    public boolean createTable() {
        try {
            execute(TableSqlParser.createTable(tableName)
                    .column(columnUser, Sql.varchar(100)).notNull().unique()
                    .column(columnSaltedHash, Sql.varchar(100)).notNull().unique()
                    .column(columnPermLevel, Sql.INT).notNull()
                    .toString()
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    public boolean removeUser(String user) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnUser + "=?)");
            statement.setString(1, user);
            statement.execute();
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        } finally {
            try {
                endTransaction(statement);
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName(), e);
            }

            close(statement);
        }
    }

    public void addNewUser(WebUser info) throws SQLException {
        addNewUser(info.getName(), info.getSaltedPassHash(), info.getPermLevel());
    }

    public void addNewUser(String user, String saltPassHash, int permLevel) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUser + ", "
                    + columnSaltedHash + ", "
                    + columnPermLevel
                    + ") VALUES (?, ?, ?)");
            statement.setString(1, user);
            statement.setString(2, saltPassHash);
            statement.setInt(3, permLevel);
            statement.execute();
            
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public boolean userExists(String user) throws SQLException {
        return getWebUser(user) != null;
    }

    public WebUser getWebUser(String user) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE (" + columnUser + "=?)");
            statement.setString(1, user);
            set = statement.executeQuery();
            if (set.next()) {
                String saltedPassHash = set.getString(columnSaltedHash);
                int permissionLevel = set.getInt(columnPermLevel);
                return new WebUser(user, saltedPassHash, permissionLevel);
            }
            return null;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public List<WebUser> getUsers() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            List<WebUser> list = new ArrayList<>();
            while (set.next()) {
                String user = set.getString(columnUser);
                String saltedPassHash = set.getString(columnSaltedHash);
                int permissionLevel = set.getInt(columnPermLevel);
                WebUser info = new WebUser(user, saltedPassHash, permissionLevel);
                list.add(info);
            }
            return list;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }
}
