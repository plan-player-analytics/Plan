/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.CriticalRunnable;

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
    private final Nickname nickname;

    public NameProcessor(UUID uuid, String playerName, String displayName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.nickname = new Nickname(displayName, System.currentTimeMillis(), ServerInfo.getServerUUID());
    }

    @Override
    public void run() {
        DataCache dataCache = DataCache.getInstance();
        String cachedName = dataCache.getName(uuid);
        String cachedDisplayName = dataCache.getDisplayName(uuid);

        boolean sameAsCached = nickname.getName().equals(cachedDisplayName);
        if (playerName.equals(cachedName) && sameAsCached) {
            return;
        }

        dataCache.updateNames(uuid, playerName, nickname.getName());

        Database database = Database.getActive();
        database.save().playerName(uuid, playerName);

        database.save().playerDisplayName(uuid, nickname);
    }
}
