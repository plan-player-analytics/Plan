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
package com.djrapitops.plan.gathering.listeners;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.gathering.listeners.nukkit.*;
import net.playeranalytics.plugin.server.Listeners;

import javax.inject.Inject;

public class NukkitListenerSystem extends ListenerSystem {

    private final Listeners listeners;

    private final Status status;
    private final PlayerOnlineListener playerOnlineListener;
    private final ChatListener chatListener;
    private final GameModeChangeListener gamemodeChangeListener;
    private final WorldChangeListener worldChangeListener;
    private final DeathEventListener deathEventListener;
    private final NukkitAFKListener afkListener;

    @Inject
    public NukkitListenerSystem(
            Listeners listeners,
            Status status,
            PlayerOnlineListener playerOnlineListener,
            ChatListener chatListener,
            GameModeChangeListener gamemodeChangeListener,
            WorldChangeListener worldChangeListener,
            DeathEventListener deathEventListener,
            NukkitAFKListener afkListener
    ) {
        this.listeners = listeners;
        this.status = status;

        this.playerOnlineListener = playerOnlineListener;
        this.chatListener = chatListener;
        this.gamemodeChangeListener = gamemodeChangeListener;
        this.worldChangeListener = worldChangeListener;
        this.deathEventListener = deathEventListener;
        this.afkListener = afkListener;
    }

    @Override
    protected void registerListeners() {
        listeners.registerListener(playerOnlineListener);
        listeners.registerListener(chatListener);
        listeners.registerListener(gamemodeChangeListener);
        listeners.registerListener(worldChangeListener);
        listeners.registerListener(deathEventListener);
        listeners.registerListener(afkListener);
        status.setCountKicks(true);
    }

    @Override
    protected void unregisterListeners() {
        listeners.unregisterListeners();
    }

    @Override
    public void callEnableEvent(PlanPlugin plugin) {
        // EnableEvent was not implemented for Nukkit. If you need it please send a PR.
    }
}
