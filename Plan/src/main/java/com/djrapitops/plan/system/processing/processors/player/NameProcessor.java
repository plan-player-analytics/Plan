/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.Actions;
import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.utilities.html.HtmlUtils;

import java.util.List;
import java.util.UUID;

/**
 * Processor for updating name in the database if the player has changed it.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class NameProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final String playerName;
    private final String displayName;

    public NameProcessor(UUID uuid, String playerName, String displayName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.displayName = displayName;
    }

    @Override
    public void run() {
        DataCache dataCache = DataCache.getInstance();
        String cachedName = dataCache.getName(uuid);
        String cachedDisplayName = dataCache.getDisplayName(uuid);

        boolean sameAsCached = displayName.equals(cachedDisplayName);
        if (playerName.equals(cachedName) && sameAsCached) {
            return;
        }

        dataCache.updateNames(uuid, playerName, displayName);

        Database database = Database.getActive();
        cueNameChangeActionProcessor(uuid, database);
        database.save().playerName(uuid, playerName);

        database.save().playerDisplayName(uuid, displayName);
    }

    private void cueNameChangeActionProcessor(UUID uuid, Database db) {
        List<String> nicknames = db.fetch().getNicknamesOfPlayerOnServer(uuid, ServerInfo.getServerUUID());
        if (nicknames.contains(displayName)) {
            return;
        }

        long time = System.currentTimeMillis();

        Processing.submitCritical(() -> {
            String info = HtmlUtils.removeXSS(displayName);

            Action action = new Action(time, Actions.NEW_NICKNAME, info);

            Database.getActive().save().action(uuid, action);
        });
    }
}