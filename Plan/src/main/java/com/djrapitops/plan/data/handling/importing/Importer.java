package main.java.com.djrapitops.plan.data.handling.importing;

import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCache;
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
import java.util.stream.IntStream;

/**
 * Abstract class used for importing data from other plugins.
 *
 * @author Rsl1122
 * @since 3.2.0
 */
@Deprecated
public abstract class Importer {

    private String info;

    /**
     * Constructor.
     */
    public Importer() {
        info = "No info specified";
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
        String processName = "Import, " + getClass().getSimpleName();
        try {
            DataCache handler = plan.getHandler();
            Database db = plan.getDB();

            Benchmark.start(processName);

            Set<UUID> saved;
            saved = db.getSavedUUIDs();

            List<UUID> unSaved = new ArrayList<>(uuids);
            unSaved.removeAll(saved);

            int amount = unSaved.size();

            String createUserObjects = "Creating " + amount + " new UserData objects";
            Log.debug(processName, createUserObjects);

            Map<UUID, IOfflinePlayer> offlinePlayers = Fetch.getIOfflinePlayers().stream().collect(Collectors.toMap(IOfflinePlayer::getUuid, Function.identity()));

            Benchmark.start(createUserObjects);

            List<UserData> newUsers = new ArrayList<>();
            List<IOfflinePlayer> offlineP = unSaved
                    .stream()
                    .map(offlinePlayers::get)
                    .collect(Collectors.toList());

            AtomicInteger currentUser = new AtomicInteger(0);
            AtomicInteger currentPercent = new AtomicInteger(0);

            int fivePercent = amount / 20;

            //Using Set because of better Collection#contains() performance
            Set<Integer> milestones = IntStream.rangeClosed(1, 20)
                    .mapToObj(i -> i * fivePercent)
                    .collect(Collectors.toSet());

            offlineP.parallelStream()
                    .map(NewPlayerCreator::createNewOfflinePlayer)
                    .forEach(newPlayer -> {
                        newUsers.add(newPlayer);
                        if (milestones.contains(currentUser.incrementAndGet())) {
                            Log.debug(processName, "Creating new UserData objects: " + currentPercent.addAndGet(5) + "%");
                        }
                    });

            Benchmark.stop(processName, createUserObjects);
            Log.debug(processName, "Save new UserData objects (" + amount + ")");

            plan.getDB().saveMultipleUserData(newUsers);

            for (UUID uuid : uuids) {
                Plan.getInstance().addToProcessQueue(importData(uuid, args));
            }
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        } finally {
            plan.getAnalysisCache().enableAnalysis();
            Log.logDebug(processName, Benchmark.stop(processName, processName));
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
            public void process(UserData uData) {
            }
        };
    }
}
