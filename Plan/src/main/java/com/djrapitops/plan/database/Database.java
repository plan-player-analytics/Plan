package main.java.com.djrapitops.plan.database;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.database.tables.*;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Abstract class representing a Database.
 * <p>
 * All methods should be only called from an asynchronous thread, unless stated
 * otherwise.
 *
 * @author Rsl1122
 */
public abstract class Database {

    /**
     * Instance of Plan used with this database.
     */
    protected final Plan plugin;

    /**
     * Table representing plan_users in the database.
     */
    protected UsersTable usersTable;

    /**
     * Table representing plan_user_info in the database.
     */
    protected UserInfoTable userInfoTable;

    /**
     * Table representing plan_actions in the database.
     */
    protected ActionsTable actionsTable;

    /**
     * Table representing plan_kills in the database.
     */
    protected KillsTable killsTable;

    /**
     * Table representing plan_nicknames in the database.
     */
    protected NicknamesTable nicknamesTable;

    /**
     * Table representing plan_sessions in the database.
     */
    protected SessionsTable sessionsTable;

    /**
     * Table representing plan_ips in the database.
     */
    protected IPsTable ipsTable;

    /**
     * Table representing plan_commandusages in the database.
     */
    protected CommandUseTable commandUseTable;

    /**
     * Table representing plan_tps in the database.
     *
     * @since 3.5.0
     */
    protected TPSTable tpsTable;

    /**
     * Table representing plan_version in the database.
     */
    protected VersionTable versionTable;

    /**
     * Table representing plan_security in the database.
     *
     * @since 3.5.2
     */
    protected SecurityTable securityTable;

    /**
     * Table representing plan_worlds in the database.
     *
     * @since 3.6.0
     */
    protected WorldTable worldTable;

    /**
     * Table representing plan_world_times in the database.
     *
     * @since 3.6.0
     */
    protected WorldTimesTable worldTimesTable;

    /**
     * Table representing plan_servers in the database.
     */
    protected ServerTable serverTable;

    protected BasicDataSource dataSource;

    /**
     * Super constructor.
     *
     * @param plugin current instance of Plan.
     */
    public Database(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Initiates the database.
     * <p>
     * Default method returns false.
     *
     * @return Was the initiation successful?
     */
    public boolean init() {
        return false;
    }

    /**
     * Used to get all UserInfo for multiple UUIDs.
     * <p>
     * Should only be called from async thread.
     *
     * @param uuids UUIDs to fetch data for.
     * @return Data for matching UUIDs.
     * @throws SQLException If database error occurs.
     */
    public abstract List<UserInfo> getUserDataForUUIDS(Collection<UUID> uuids) throws SQLException;

    /**
     * Check if the user is saved in the database.
     *
     * @param uuid UUID of the user.
     * @return true/false
     */
    public abstract boolean wasSeenBefore(UUID uuid);

    /**
     * Cleans the database of excess data.
     */
    public abstract void clean();

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
        return getName().toLowerCase().replace(" ", "");
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
     * Closes the database & it's resources.
     *
     * @throws SQLException If a database error occurs.
     */
    public abstract void close() throws SQLException;

    /**
     * Removes all data related to an account from the database.
     *
     * @param uuid UUID of the account.
     * @return Success of the removal.
     * @throws SQLException If a database error occurs.
     */
    public abstract boolean removeAccount(UUID uuid) throws SQLException;

    /**
     * Used to clear all data from the database.
     * <p>
     * Uses DELETE * FROM table.
     *
     * @return Success of removal.
     */
    public abstract boolean removeAllData();

    /**
     * Used to save CommandUse map.
     *
     * @param data String command (key), Integer times used
     * @throws SQLException         If a database error occurs.
     * @throws NullPointerException If the database has not initialized tables.
     */
    public void saveCommandUse(Map<String, Integer> data) throws SQLException {
        commandUseTable.saveCommandUse(data);
    }

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

    public BasicDataSource getDataSource() {
        return dataSource;
    }

    public abstract void commit(Connection connection) throws SQLException;
}
