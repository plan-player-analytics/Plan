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
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plan.system.processing.Processing;
import me.konsolas.aac.api.AACAPIProvider;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationCommandEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Class responsible for listening kick events made by AAC.
 *
 * @author Rsl1122
 * @since 4.1.0
 */
public class PlayerHackKickListener implements Listener {

    private final HackerTable hackerTable;
    private final Processing processing;

    PlayerHackKickListener(HackerTable hackerTable, Processing processing) {
        this.hackerTable = hackerTable;
        this.processing = processing;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerViolationCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        HackType hackType = event.getHackType();
        String hackTypeName = hackType.getName();
        long time = System.currentTimeMillis();
        int violations = AACAPIProvider.getAPI().getViolationLevel(player, hackType);

        HackObject hackObject = new HackObject(uuid, time, hackTypeName, violations);

        processing.submitNonCritical(() -> hackerTable.insertHackRow(hackObject));
    }
}
