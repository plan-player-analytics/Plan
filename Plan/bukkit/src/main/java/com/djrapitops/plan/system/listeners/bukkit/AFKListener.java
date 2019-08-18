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
package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.system.afk.AFKTracker;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener that keeps track of actions that are not considered being AFK.
 * <p>
 * Additional Listener calls in PlayerOnlineListener to avoid having HIGHEST priority listeners.
 *
 * @author Rsl1122
 * @see PlayerOnlineListener
 */
public class AFKListener implements Listener {

    // Static so that /reload does not cause afk tracking to fail.
    static AFKTracker AFK_TRACKER;

    private final Map<UUID, Boolean> ignorePermissionInfo;
    private final ErrorHandler errorHandler;

    @Inject
    public AFKListener(PlanConfig config, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.ignorePermissionInfo = new HashMap<>();

        AFKListener.assignAFKTracker(config);
    }

    private static void assignAFKTracker(PlanConfig config) {
        if (AFK_TRACKER == null) {
            AFK_TRACKER = new AFKTracker(config);
        }
    }

    private void event(PlayerEvent event) {
        try {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            long time = System.currentTimeMillis();

            Boolean ignored = ignorePermissionInfo.get(uuid);
            if (ignored == null) {
                ignored = player.hasPermission(Permissions.IGNORE_AFK.getPermission());
            }
            if (ignored) {
                AFK_TRACKER.hasIgnorePermission(uuid);
                ignorePermissionInfo.put(uuid, true);
                return;
            } else {
                ignorePermissionInfo.put(uuid, false);
            }

            AFK_TRACKER.performedAction(uuid, time);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        event(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        event(event);
        boolean isAfkCommand = event.getMessage().substring(1).toLowerCase().startsWith("afk");
        if (isAfkCommand) {
            UUID uuid = event.getPlayer().getUniqueId();
            AFK_TRACKER.usedAfkCommand(uuid, System.currentTimeMillis());
        }
    }

}