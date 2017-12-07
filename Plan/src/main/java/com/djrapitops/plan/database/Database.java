package main.java.com.djrapitops.plan.database;

import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.data.PlayerProfile;
import main.java.com.djrapitops.plan.data.ServerProfile;
import main.java.com.djrapitops.plan.database.tables.*;
import org.apache.commons.lang3.StringUtils;

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



    /**
     * Super constructor.
     */
    public Database() {
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
        return StringUtils.remove(getName().toLowerCase(), ' ');
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
    public abstract void removeAccount(UUID uuid) throws SQLException;

    /**
     * Used to clear all data from the database.
     * <p>
     * Uses DELETE * FROM table.
     *
     * @throws SQLException if remove fails.
     */
    public abstract void removeAllData() throws SQLException;

    /**
     * Used to fetch the saved UUIDs in the users table.
     *
     * @return Set of saved UUIDs
     * @throws SQLException If a database error occurs.
     */
    public Set<UUID> getSavedUUIDs() throws SQLException {
        return usersTable.getSavedUUIDs();
    }

    /**
     * Used to get the Command usage mep.
     *
     * @return String command (key), Integer times used
     * @throws SQLException If a database error occurs.
     */
    public Map<String, Integer> getCommandUse() throws SQLException {
        return commandUseTable.getCommandUse();
    }

    /**
     * Used to get the users table.
     *
     * @return Table representing plan_users
     */
    public UsersTable getUsersTable() {
        return usersTable;
    }

    /**
     * Used to get the users table.
     *
     * @return Table representing plan_sessions
     */
    public SessionsTable getSessionsTable() {
        return sessionsTable;
    }

    /**
     * Used to get the kills table.
     *
     * @return Table representing plan_kills
     */
    public KillsTable getKillsTable() {
        return killsTable;
    }

    /**
     * Used to get the ips table.
     *
     * @return Table representing plan_ips
     */
    public IPsTable getIpsTable() {
        return ipsTable;
    }

    /**
     * Used to get the nicknames table.
     *
     * @return Table representing plan_nicknames
     */
    public NicknamesTable getNicknamesTable() {
        return nicknamesTable;
    }

    /**
     * Used to get the command usage table.
     *
     * @return Table representing plan_commandusages
     */
    public CommandUseTable getCommandUseTable() {
        return commandUseTable;
    }

    /**
     * Used to get the tps table.
     *
     * @return Table representing plan_tps
     */
    public TPSTable getTpsTable() {
        return tpsTable;
    }

    /**
     * Used to get the security table.
     *
     * @return Table representing plan_security
     */
    public SecurityTable getSecurityTable() {
        return securityTable;
    }

    /**
     * Used to get the worlds table.
     *
     * @return Table representing plan_worlds
     */
    public WorldTable getWorldTable() {
        return worldTable;
    }

    /**
     * Used to get the world times table.
     *
     * @return Table representing plan_world_times
     */
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

    public abstract void commit(Connection connection) throws SQLException;

    public boolean isUsingMySQL() {
        return "mysql".equals(getConfigName());
    }

    public abstract PlayerProfile getPlayerProfile(UUID uuid) throws SQLException;

    public abstract ServerProfile getServerProfile(UUID serverUUID) throws SQLException;
}
