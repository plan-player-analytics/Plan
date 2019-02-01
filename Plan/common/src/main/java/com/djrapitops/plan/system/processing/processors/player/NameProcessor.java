/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.processing.CriticalRunnable;

import java.util.UUID;

/**
 * Processor for updating name in the database if the player has changed it.
 *
 * @author Rsl1122
 */
public class NameProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final Nickname nickname;

    private final Database database;
    private final DataCache dataCache;

    NameProcessor(
            UUID uuid, Nickname nickname,
            Database database,
            DataCache dataCache
    ) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.database = database;
        this.dataCache = dataCache;
    }

    @Override
    public void run() {
        String cachedDisplayName = dataCache.getDisplayName(uuid);

        boolean sameAsCached = nickname.getName().equals(cachedDisplayName);
        if (sameAsCached) {
            return;
        }

        dataCache.updateDisplayName(uuid, nickname.getName());

        database.save().playerDisplayName(uuid, nickname);
    }
}
