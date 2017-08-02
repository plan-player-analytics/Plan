package main.java.com.djrapitops.plan.data.handling.importing;

import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import com.djrapitops.plugin.utilities.status.ProcessStatus;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.InfoType;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.NewPlayerCreator;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * <p>
     * Creates UserData for players that have not been saved to the database.
     *
     * @param uuids UUIDs to be imported
     * @param args  arguments for the import
     * @return success
     */
    public boolean importData(Collection<UUID> uuids, String... args) {
        Plan plan = Plan.getInstance();
        plan.getAnalysisCache().disableAnalysisTemporarily();

        try {
            String processName = "Import, " + getClass().getSimpleName();

            ProcessStatus<Plan> processStatus = plan.processStatus();
            DataCacheHandler handler = plan.getHandler();
            Database db = plan.getDB();

            processStatus.startExecution(processName);

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
            processStatus.setStatus(processName, createUserObjects);

            Map<UUID, IOfflinePlayer> offlinePlayers = Fetch.getIOfflinePlayers().stream().collect(Collectors.toMap(IOfflinePlayer::getUuid, Function.identity()));

            Benchmark.start(createUserObjects);

            List<UserData> newUsers = new ArrayList<>();
            List<IOfflinePlayer> offlineP = unSaved
                    .stream()
                    .map(offlinePlayers::get)
                    .collect(Collectors.toList());

            AtomicInteger currentUser = new AtomicInteger(0);
            int amount = unSaved.size();

            offlineP.parallelStream()
                    .map(NewPlayerCreator::createNewOfflinePlayer)
                    .forEach(newPlayer -> {
                        newPlayer.setLastPlayed(newPlayer.getRegistered());
                        newUsers.add(newPlayer);
                        processStatus.setStatus(processName, "Creating new UserData objects: " + currentUser.addAndGet(1) + "/" + amount);
                    });

            Benchmark.stop(createUserObjects);
            processStatus.setStatus(processName, "Save new UserData objects (" + amount + ")");

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
