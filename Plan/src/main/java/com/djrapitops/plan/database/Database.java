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
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Abstract class representing a Database.
 *
 * @author Rsl1122
 */
public abstract class Database {

    protected final Plan plugin;
    protected UsersTable usersTable;
    protected GMTimesTable gmTimesTable;
    protected KillsTable killsTable;
    protected LocationsTable locationsTable;
    protected NicknamesTable nicknamesTable;
    protected SessionsTable sessionsTable;
    protected IPsTable ipsTable;
    protected CommandUseTable commandUseTable;
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

    public abstract List<UserData> getUserDataForUUIDS(Collection<UUID> uuids) throws SQLException;

    /**
     * Used to save UserData object of a user.
     *
     * @param uuid UUID of the player
     * @param data UserData of the Player.
     * @throws SQLException If a database error occurs.
     * @deprecated Separate UUID no longer required.
     */
    @Deprecated
    public void saveUserData(UUID uuid, UserData data) throws SQLException {
        if (uuid.equals(data.getUuid())) {
            saveUserData(data);
        }
    }

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
     * @return SQLite/MySQL
     */
    public abstract String getName();

    /**
     * Used to get the config name of the database type.
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
     * @param data
     * @throws SQLException If a database error occurs.
     * @throws NullPointerException If the database has not initialized tables.
     */
    @Deprecated
    public void saveCommandUse(Map<String, Integer> data) throws SQLException, NullPointerException {
        commandUseTable.saveCommandUse(data);
    }

    /**
     *
     * @return @throws SQLException If a database error occurs.
     */
    public Set<UUID> getSavedUUIDs() throws SQLException {
        return usersTable.getSavedUUIDs();
    }

    /**
     *
     * @return @throws SQLException If a database error occurs.
     */
    @Deprecated
    public Map<String, Integer> getCommandUse() throws SQLException {
        return commandUseTable.getCommandUse();
    }

    /**
     *
     * @param uuid
     * @return
     * @throws SQLException If a database error occurs.
     */
    @Deprecated
    public int getUserId(String uuid) throws SQLException {
        return usersTable.getUserId(uuid);
    }

    /**
     *
     * @param userId
     * @param worlds
     * @return
     * @throws SQLException If a database error occurs.
     */
    @Deprecated
    public List<Location> getLocations(String userId, HashMap<String, World> worlds) throws SQLException {
        return getLocations(Integer.parseInt(userId), worlds);
    }

    @Deprecated
    public List<Location> getLocations(int userId, HashMap<String, World> worlds) throws SQLException {
        return locationsTable.getLocations(userId, worlds);
    }

    /**
     *
     * @return
     */
    public UsersTable getUsersTable() {
        return usersTable;
    }

    /**
     *
     * @return
     */
    public SessionsTable getSessionsTable() {
        return sessionsTable;
    }

    /**
     *
     * @return
     */
    public GMTimesTable getGmTimesTable() {
        return gmTimesTable;
    }

    /**
     *
     * @return
     */
    public KillsTable getKillsTable() {
        return killsTable;
    }

    /**
     *
     * @return
     */
    public LocationsTable getLocationsTable() {
        return locationsTable;
    }

    /**
     *
     * @return
     */
    public IPsTable getIpsTable() {
        return ipsTable;
    }

    /**
     *
     * @return
     */
    public NicknamesTable getNicknamesTable() {
        return nicknamesTable;
    }

    /**
     *
     * @return
     */
    public CommandUseTable getCommandUseTable() {
        return commandUseTable;
    }
}
