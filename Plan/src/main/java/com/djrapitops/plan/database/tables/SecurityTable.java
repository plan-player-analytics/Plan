/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Insert;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rsl1122
 */
public class SecurityTable extends Table {

    private final String columnUser = "username";
    private final String columnSaltedHash = "salted_pass_hash";
    private final String columnPermLevel = "permission_level";
    private String insertStatement;

    public SecurityTable(SQLDB db, boolean usingMySQL) {
        super("plan_security", db, usingMySQL);
        insertStatement = Insert.values(tableName,
                columnUser,
                columnSaltedHash,
                columnPermLevel);
    }

    @Override
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnUser, Sql.varchar(100)).notNull().unique()
                .column(columnSaltedHash, Sql.varchar(100)).notNull().unique()
                .column(columnPermLevel, Sql.INT).notNull()
                .toString()
        );
    }

    public boolean removeUser(String user) {
        PreparedStatement statement = null;
        try (Connection connection = getConnection()){
            statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnUser + "=?)");
            statement.setString(1, user);

            statement.execute();
            connection.commit();
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        } finally {
            close(statement);
        }
    }

    public void addNewUser(WebUser info) throws SQLException {
        addNewUser(info.getName(), info.getSaltedPassHash(), info.getPermLevel());
    }

    public void addNewUser(String user, String saltPassHash, int permLevel) throws SQLException {
        PreparedStatement statement = null;
        try (Connection connection = getConnection()){
            statement = connection.prepareStatement(insertStatement);
            statement.setString(1, user);
            statement.setString(2, saltPassHash);
            statement.setInt(3, permLevel);
            statement.execute();

            connection.commit();
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
        try (Connection connection = getConnection()){
            statement = connection.prepareStatement(Select.all(tableName).where(columnUser + "=?").toString());
            statement.setString(1, user);
            set = statement.executeQuery();
            if (set.next()) {
                String saltedPassHash = set.getString(columnSaltedHash);
                int permissionLevel = set.getInt(columnPermLevel);
                return new WebUser(user, saltedPassHash, permissionLevel);
            }
            return null;
        } finally {
            close(set, statement);
        }
    }

    public List<WebUser> getUsers() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try (Connection connection = getConnection()){
            statement = connection.prepareStatement(Select.all(tableName).toString());
            statement.setFetchSize(5000);
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
            close(set, statement);
        }
    }

    public void addUsers(List<WebUser> users) throws SQLException {
        if (Verify.isEmpty(users)) {
            return;
        }
        PreparedStatement statement = null;
        try (Connection connection = getConnection()){
            statement = connection.prepareStatement(insertStatement);
            for (WebUser user : users) {
                String userName = user.getName();
                String pass = user.getSaltedPassHash();
                int permLvl = user.getPermLevel();

                statement.setString(1, userName);
                statement.setString(2, pass);
                statement.setInt(3, permLvl);
                statement.addBatch();
            }

            statement.executeBatch();
            connection.commit();
        } finally {
            close(statement);
        }
    }
}
