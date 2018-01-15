/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.ProcessingQueue;
import com.djrapitops.plan.system.processing.processors.NewNickActionProcessor;
import com.djrapitops.plan.systems.cache.DataCache;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.List;
import java.util.UUID;

/**
 * Processor for updating name in the database if the player has changed it.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class NameProcessor extends PlayerProcessor {

    private final String playerName;
    private final String displayName;

    public NameProcessor(UUID uuid, String playerName, String displayName) {
        super(uuid);
        this.playerName = playerName;
        this.displayName = displayName;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        Plan plugin = Plan.getInstance();
        DataCache dataCache = plugin.getDataCache();
        String cachedName = dataCache.getName(uuid);
        String cachedDisplayName = dataCache.getDisplayName(uuid);

        boolean sameAsCached = displayName.equals(cachedDisplayName);
        if (playerName.equals(cachedName) && sameAsCached) {
            return;
        }

        Database database = Database.getActive();
        try {
            cueNameChangeActionProcessor(uuid, database);
            database.save().playerName(uuid, playerName);

            database.save().playerDisplayName(uuid, displayName);
        } catch (DBException e) {
            Log.toLog(this.getClass().getName(), e);
        }

        dataCache.updateNames(uuid, playerName, displayName);
    }

    private void cueNameChangeActionProcessor(UUID uuid, Database db) throws DBException {
        List<String> nicknames = db.fetch().getNicknamesOfPlayerOnServer(uuid, Plan.getServerUUID());
        if (nicknames.contains(displayName)) {
            return;
        }
        ProcessingQueue.getInstance().queue(new NewNickActionProcessor(uuid, displayName));
    }
}