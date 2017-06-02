package main.java.com.djrapitops.plan.database;

import java.sql.SQLException;
import java.util.*;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.database.tables.CommandUseTable;
import main.java.com.djrapitops.plan.database.tables.GMTimesTable;
import main.java.com.djrapitops.plan.database.tables.IPsTable;
import main.java.com.djrapitops.plan.database.tables.KillsTable;
import main.java.com.djrapitops.plan.database.tables.LocationsTable;
import main.java.com.djrapitops.plan.database.tables.NicknamesTable;
import main.java.com.djrapitops.plan.database.tables.SessionsTable;
import main.java.com.djrapitops.plan.database.tables.UsersTable;
import main.java.com.djrapitops.plan.database.tables.VersionTable;

/**
 * Abstract class representing a Database.
 *
 * All methods should be only called from an asyncronous thread, unless stated
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
     * Table representing plan_gamemodetimes in the database.
     */
    protected GMTimesTable gmTimesTable;

    /**
     * Table representing plan_kills in the database.
     */
    protected KillsTable killsTable;

    /**
     * Table representing plan_locations in the database.
     */
    protected LocationsTable locationsTable;

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
     * Table representing plan_version in the database.
     */
    protected VersionTable versionTable;

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
     *
     * Default method returns false.
     *
     * @return Was the initiation successful?
     */
    public boolean init() {
        return false;
    }

    /**
     * Used to give Database processors to call with UserData after they have
     * been fetched from the database.
     *
     * This method is a shortcut method for multiple parameters.
     *
     * @param uuid UUID of the player.
     * @param processors Processors to call with the UserData after the fetch is
     * complete.
     * @throws SQLException If a database error occurs.
     */
    public void giveUserDataToProcessors(UUID uuid, DBCallableProcessor... processors) throws SQLException {
        giveUserDataToProcessors(uuid, Arrays.asList(processors));
    }

    /**
     * Used to give Database processors to call with UserData after they have
     * been fetched from the database.
     *
     * @param uuid UUID of the player.
     * @param processors Processors to call with the UserData after the fetch is
     * complete.
     * @throws SQLException If a database error occurs.
     */
    public abstract void giveUserDataToProcessors(UUID uuid, Collection<DBCallableProcessor> processors) throws SQLException;

    /**
     * Used to get all UserData for multiple UUIDs.
     *
     * Should only be called from async thread.
     *
     * @param uuids UUIDs to fetch data for.
     * @return Data for matching UUIDs.
     * @throws SQLException If database error occurs.
     */
    public abstract List<UserData> getUserDataForUUIDS(Collection<UUID> uuids) throws SQLException;

    /**
     * Used to save UserData object of a user.
     *
     * @param data UserData of the Player.
     * @throws SQLException If a database error occurs.
     */
    public abstract void saveUserData(UserData data) throws SQLException;

    /**
     * Used to save UserData object of multiple users.
     *
     * @param data Collection of UserData objects.
     * @throws SQLException If a database error occurs.
     */
    public abstract void saveMultipleUserData(Collection<UserData> data) throws SQLException;

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
     *
     * Thread safe.
     *
     * @return SQLite/MySQL
     */
    public abstract String getName();

    /**
     * Used to get the config name of the database type.
     *
     * Thread safe.
     *
     * @return sqlite/mysql
     */
    public String getConfigName() {
        return getName().toLowerCase().replace(" ", "");
    }

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
     * updated.
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
    public abstract boolean removeAccount(String uuid) throws SQLException;

    /**
     * Used to clear all data from the database.
     *
     * Uses DELETE * FROM table.
     *
     * @return Success of removal.
     * @throws SQLException If a database error occurs.
     */
    public abstract boolean removeAllData() throws SQLException;

    /**
     * Used to save CommandUse map.
     *
     * @param data String command (key), Integer times used
     * @throws SQLException If a database error occurs.
     * @throws NullPointerException If the database has not initialized tables.
     */
    public void saveCommandUse(Map<String, Integer> data) throws SQLException, NullPointerException {
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
     * Used to get the gm times table.
     *
     * @return Table representing plan_gamemodetimes
     */
    public GMTimesTable getGmTimesTable() {
        return gmTimesTable;
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
     * Used to get the locations table.
     *
     * @return Table representing plan_locations
     */
    public LocationsTable getLocationsTable() {
        return locationsTable;
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
}
