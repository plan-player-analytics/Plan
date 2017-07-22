package main.java.com.djrapitops.plan.data.handling.importing;

import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.InfoType;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.NewPlayerCreator;

/**
 * Abstract class used for importing data from other plugins.
 *
 * @author Rsl1122
 * @since 3.2.0
 */
public abstract class Importer {

    private String info;

    /**
     * Constructor.
     */
    public Importer() {
        info = "No info specified";
    }

    /**
     * Import data from users.
     *
     * @param uuids UUIDs of players to import
     * @return Success of import
     * @deprecated Use importData(Collection, String...) instead (new system)
     */
    @Deprecated
    public boolean importData(Collection<UUID> uuids) {
        return importData(uuids, new String[0]);
    }

    /**
     * Method used for the import.
     *
     * Creates UserData for players that have not been saved to the database.
     *
     * @param uuids UUIDs to be imported
     * @param args arguments for the import
     * @return success
     */
    public boolean importData(Collection<UUID> uuids, String... args) {
        Plan plan = Plan.getInstance();
        plan.getAnalysisCache().disableAnalysisTemporarily();
        try {
            String processName = "Import, " + getClass().getSimpleName();
            plan.processStatus().startExecution(processName);
            DataCacheHandler handler = plan.getHandler();
            Database db = plan.getDB();
            Set<UUID> saved;
            try {
                saved = db.getSavedUUIDs();
            } catch (SQLException ex) {
                Log.toLog(this.getClass().getName(), ex);
                return false;
            }
            List<UUID> unSaved = new ArrayList<>(uuids);
            unSaved.removeAll(saved);
            String createUserObjects = "Creating new UserData objects for: " + unSaved.size();
            plan.processStatus().setStatus(processName, createUserObjects);
            Map<UUID, IOfflinePlayer> offlinePlayers = Fetch.getIOfflinePlayers().stream().collect(Collectors.toMap(IOfflinePlayer::getUuid, Function.identity()));
            Benchmark.start(createUserObjects);
            List<IOfflinePlayer> offlineP = unSaved.stream().map(uuid
                    -> offlinePlayers.get(uuid)).collect(Collectors.toList());
            List<UserData> newUsers = new ArrayList<>();
            for (IOfflinePlayer p : offlineP) {
                UserData newPlayer = NewPlayerCreator.createNewOfflinePlayer(p);
                newPlayer.setLastPlayed(newPlayer.getRegistered());
                newUsers.add(newPlayer);
                plan.processStatus().setStatus(processName, "Creating new UserData objects: " + newUsers.size() + "/" + unSaved.size());
            }
            Benchmark.stop(createUserObjects);
            plan.processStatus().setStatus(processName, "Save new UserData objects (" + unSaved.size() + ")");
            try {
                plan.getDB().saveMultipleUserData(newUsers);
            } catch (SQLException ex) {
                Log.toLog(this.getClass().getName(), ex);
            }
            for (UUID uuid : uuids) {
                handler.addToPool(importData(uuid, args));
            }
            plan.processStatus().finishExecution(processName);
        } finally {
            plan.getAnalysisCache().enableAnalysis();
        }
        return true;
    }

    /**
     * Returns the info for import command.
     *
     * @return Information about the import options
     * @since 3.5.0
     */
    public final String getInfo() {
        return info;
    }

    /**
     * Set the info for import command.
     *
     * @param info Information about the import options
     * @since 3.5.0
     */
    public final void setInfo(String info) {
        this.info = info;
    }

    /**
     * Import data of a single player.
     *
     * @param uuid UUID of the player
     * @return HandlingInfo used to modify saved userdata.
     * @deprecated Deprecated (new system), use importData(UUID, String...)
     * instead
     */
    @Deprecated
    public HandlingInfo importData(UUID uuid) {
        return importData(uuid, new String[0]);
    }

    /**
     * Method used for getting the HandlingInfo object for the import data.
     *
     * @param uuid UUID of the player
     * @param args Arguments for import
     * @return HandlingInfo object that modifies the UserData so that the data
     * is imported.
     * @since 3.5.0
     */
    public HandlingInfo importData(UUID uuid, String... args) {
        return new HandlingInfo(uuid, InfoType.OTHER, 0) {
            @Override
            public boolean process(UserData uData) {
                return true;
            }
        };
    }
}
