package com.djrapitops.plan.system.database.databases;

import com.djrapitops.plan.api.exceptions.DatabaseInitException;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.tables.*;
import com.djrapitops.plan.utilities.NullCheck;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract class representing a Database.
 * <p>
 * All methods should be only called from an asynchronous thread, unless stated
 * otherwise.
 *
 * @author Rsl1122
 */
public abstract class Database {

    protected UsersTable usersTable;
    protected UserInfoTable userInfoTable;
    protected ActionsTable actionsTable;
    protected KillsTable killsTable;
    protected NicknamesTable nicknamesTable;
    protected SessionsTable sessionsTable;
    protected IPsTable ipsTable;
    protected CommandUseTable commandUseTable;
    protected TPSTable tpsTable;
    protected VersionTable versionTable;
    protected SecurityTable securityTable;
    protected WorldTable worldTable;
    protected WorldTimesTable worldTimesTable;
    protected ServerTable serverTable;

    public static Database getInstance() {
        Database database = DBSystem.getInstance().getActiveDatabase();
        NullCheck.check(database, new IllegalStateException("Database was not initialized."));
        return database;
    }

    /**
     * Initiates the database.
     *
     * @throws DatabaseInitException if SQLException or other exception occurs.
     */
    public void init() throws DatabaseInitException {
    }

    /**
     * Condition if the user is saved in the database.
     *
     * @param uuid UUID of the user.
     * @return true/false
     */
    public abstract boolean wasSeenBefore(UUID uuid);

    /**
     * Used to get the name of the database type.
     * <p>
     * Thread safe.
     *
     * @return SQLite/MySQL
     */
    public abstract String getName();

    /**
     * Used to get the config name of the database type.
     * <p>
     * Thread safe.
     *
     * @return sqlite/mysql
     */
    public String getConfigName() {
        return getName().toLowerCase().trim();
    }

    public abstract boolean isNewDatabase() throws SQLException;

    /**
     * Used to get the database schema version.
     *
     * @return Integer starting from 0, incremented by one when schema is
     * updated.
     * @throws SQLException If a database error occurs.
     */
    public abstract int getVersion() throws SQLException;

    /**
     * Used to set the database schema version.
     *
     * @param version Integer starting from 0, incremented by one when schema is
     *                updated.
     * @throws SQLException If a database error occurs.
     */
    public abstract void setVersion(int version) throws SQLException;

    /**
     * Closes the database and it's resources.
     *
     * @throws SQLException If a database error occurs.
     */
    public abstract void close() throws SQLException;

    /**
     * Returns a connection to the MySQL connection pool.
     * <p>
     * On SQLite does nothing.
     *
     * @param connection Connection to return.
     * @throws SQLException DB Error
     */
    public abstract void returnToPool(Connection connection) throws SQLException;

    /**
     * Removes all data related to an account from the database.
     *
     * @param uuid UUID of the account.
     * @throws SQLException If a database error occurs.
     */
    @Deprecated
    public abstract void removeAccount(UUID uuid) throws SQLException;

    /**
     * Used to clear all data from the database.
     * <p>
     * Uses DELETE * FROM table.
     *
     * @throws SQLException if remove fails.
     */
    @Deprecated
    public abstract void removeAllData() throws SQLException;

    /**
     * Used to fetch the saved UUIDs in the users table.
     *
     * @return Set of saved UUIDs
     * @throws SQLException If a database error occurs.
     */
    @Deprecated
    public Set<UUID> getSavedUUIDs() throws SQLException {
        return usersTable.getSavedUUIDs();
    }

    /**
     * Used to get the Command usage mep.
     *
     * @return String command (key), Integer times used
     * @throws SQLException If a database error occurs.
     */
    @Deprecated
    public Map<String, Integer> getCommandUse() throws SQLException {
        return commandUseTable.getCommandUse();
    }


    public abstract void commit(Connection connection) throws SQLException;

    public boolean isUsingMySQL() {
        return "mysql".equals(getConfigName());
    }

    @Deprecated
    public abstract PlayerProfile getPlayerProfile(UUID uuid) throws SQLException;

    @Deprecated
    public abstract ServerProfile getServerProfile(UUID serverUUID) throws SQLException;

    public UsersTable getUsersTable() {
        return usersTable;
    }

    public SessionsTable getSessionsTable() {
        return sessionsTable;
    }

    public KillsTable getKillsTable() {
        return killsTable;
    }

    public IPsTable getIpsTable() {
        return ipsTable;
    }

    public NicknamesTable getNicknamesTable() {
        return nicknamesTable;
    }

    public CommandUseTable getCommandUseTable() {
        return commandUseTable;
    }

    public TPSTable getTpsTable() {
        return tpsTable;
    }

    public SecurityTable getSecurityTable() {
        return securityTable;
    }

    public WorldTable getWorldTable() {
        return worldTable;
    }

    public WorldTimesTable getWorldTimesTable() {
        return worldTimesTable;
    }

    public ServerTable getServerTable() {
        return serverTable;
    }

    public ActionsTable getActionsTable() {
        return actionsTable;
    }

    public UserInfoTable getUserInfoTable() {
        return userInfoTable;
    }
}
