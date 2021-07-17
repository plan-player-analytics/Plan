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
package net.playeranalytics.plan.gathering.listeners;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import net.playeranalytics.plan.gathering.listeners.fabric.*;
import net.playeranalytics.plugin.server.Listeners;

import javax.inject.Inject;

/**
 * Listener system for the Fabric platform.
 *
 * @author Kopo942
 */
public class FabricListenerSystem extends ListenerSystem {

    private final ChatListener chatListener;
    private final DeathEventListener deathEventListener;
    private final FabricAFKListener fabricAFKListener;
    private final GameModeChangeListener gameModeChangeListener;
    private final PlayerOnlineListener playerOnlineListener;
    private final WorldChangeListener worldChangeListener;
    private final Listeners listeners;

    @Inject
    public FabricListenerSystem(
            ChatListener chatListener,
            DeathEventListener deathEventListener,
            FabricAFKListener fabricAFKListener,
            GameModeChangeListener gameModeChangeListener,
            PlayerOnlineListener playerOnlineListener,
            WorldChangeListener worldChangeListener,
            Listeners listeners
            ) {
        this.chatListener = chatListener;
        this.deathEventListener = deathEventListener;
        this.fabricAFKListener = fabricAFKListener;
        this.playerOnlineListener = playerOnlineListener;
        this.gameModeChangeListener = gameModeChangeListener;
        this.worldChangeListener = worldChangeListener;
        this.listeners = listeners;

    }

    @Override
    protected void registerListeners() {
        listeners.registerListener(chatListener);
        listeners.registerListener(deathEventListener);
        listeners.registerListener(fabricAFKListener);
        listeners.registerListener(gameModeChangeListener);
        listeners.registerListener(playerOnlineListener);
        listeners.registerListener(worldChangeListener);
    }

    @Override
    protected void unregisterListeners() {
        listeners.unregisterListener(chatListener);
        listeners.unregisterListener(deathEventListener);
        listeners.unregisterListener(fabricAFKListener);
        listeners.unregisterListener(gameModeChangeListener);
        listeners.unregisterListener(playerOnlineListener);
        listeners.unregisterListener(worldChangeListener);
    }

    @Override
    public void callEnableEvent(PlanPlugin plugin) {
        // TODO implement
    }
}
