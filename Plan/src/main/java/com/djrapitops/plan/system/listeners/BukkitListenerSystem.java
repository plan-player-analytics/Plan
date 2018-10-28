/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.listeners.bukkit.*;
import org.bukkit.event.HandlerList;

import javax.inject.Inject;

public class BukkitListenerSystem extends ListenerSystem {

    private final Plan plugin;

    private final PlayerOnlineListener playerOnlineListener;
    private final ChatListener chatListener;
    private final GameModeChangeListener gamemodeChangeListener;
    private final WorldChangeListener worldChangeListener;
    private final CommandListener commandListener;
    private final DeathEventListener deathEventListener;
    private final AFKListener afkListener;

    @Inject
    public BukkitListenerSystem(Plan plugin,
                                PlayerOnlineListener playerOnlineListener,
                                ChatListener chatListener,
                                GameModeChangeListener gamemodeChangeListener,
                                WorldChangeListener worldChangeListener,
                                CommandListener commandListener,
                                DeathEventListener deathEventListener,
                                AFKListener afkListener
    ) {
        this.plugin = plugin;

        this.playerOnlineListener = playerOnlineListener;
        this.chatListener = chatListener;
        this.gamemodeChangeListener = gamemodeChangeListener;
        this.worldChangeListener = worldChangeListener;
        this.commandListener = commandListener;
        this.deathEventListener = deathEventListener;
        this.afkListener = afkListener;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(
                playerOnlineListener,
                chatListener,
                gamemodeChangeListener,
                worldChangeListener,
                commandListener,
                deathEventListener,
                afkListener
        );
        PlayerOnlineListener.setCountKicks(true);
    }

    @Override
    protected void unregisterListeners() {
        HandlerList.unregisterAll(plugin);
    }
}
