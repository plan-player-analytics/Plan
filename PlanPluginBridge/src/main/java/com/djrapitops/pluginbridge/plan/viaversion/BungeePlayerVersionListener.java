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
package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plan.db.Database;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import us.myles.ViaVersion.api.ViaAPI;

import java.util.UUID;

/**
 * Class responsible for listening join events for Version protocol.
 *
 * @author Rsl1122

 */
public class BungeePlayerVersionListener implements Listener {

    private final ViaAPI viaAPI;

    private final Database database;

    BungeePlayerVersionListener(
            ViaAPI viaAPI,
            Database database
    ) {
        this.viaAPI = viaAPI;
        this.database = database;
    }

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int playerVersion = viaAPI.getPlayerVersion(uuid);
        database.executeTransaction(new StoreUsedProtocolTransaction(uuid, playerVersion));
    }
}
