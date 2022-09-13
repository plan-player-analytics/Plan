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
import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.SpongeServerShutdownSave;
import com.djrapitops.plan.api.events.PlanSpongeEnableEvent;
import com.djrapitops.plan.capability.CapabilitySvc;
import com.djrapitops.plan.gathering.listeners.sponge.*;
import net.playeranalytics.plugin.server.Listeners;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;

import javax.inject.Inject;

public class SpongeListenerSystem extends ListenerSystem {

    private final Listeners listeners;

    private final SpongeAFKListener afkListener;
    private final SpongeChatListener chatListener;
    private final SpongeDeathListener deathListener;
    private final SpongeGMChangeListener gmChangeListener;
    private final PlayerOnlineListener playerListener;
    private final SpongeWorldChangeListener worldChangeListener;
    private final SpongeServerShutdownSave spongeServerShutdownSave;

    @Inject
    public SpongeListenerSystem(
            Listeners listeners,
            SpongeAFKListener afkListener,
            SpongeChatListener chatListener,
            SpongeDeathListener deathListener,
            SpongeGMChangeListener gmChangeListener,
            PlayerOnlineListener playerListener,
            SpongeWorldChangeListener worldChangeListener,
            SpongeServerShutdownSave spongeServerShutdownSave
    ) {
        this.listeners = listeners;

        this.afkListener = afkListener;
        this.chatListener = chatListener;
        this.deathListener = deathListener;
        this.gmChangeListener = gmChangeListener;
        this.playerListener = playerListener;
        this.worldChangeListener = worldChangeListener;
        this.spongeServerShutdownSave = spongeServerShutdownSave;
    }

    @Override
    protected void registerListeners() {
        listeners.registerListener(afkListener);
        listeners.registerListener(chatListener);
        listeners.registerListener(deathListener);
        listeners.registerListener(playerListener);
        listeners.registerListener(gmChangeListener);
        listeners.registerListener(worldChangeListener);
        listeners.registerListener(spongeServerShutdownSave);
    }

    @Override
    protected void unregisterListeners() {
        listeners.unregisterListeners();
    }

    @Override
    public void callEnableEvent(PlanPlugin plugin) {
        try {
            Event event = new PlanSpongeEnableEvent((PlanSponge) plugin);
            Sponge.eventManager().post(event);
        } catch (IllegalStateException ignore) {
            /* Ignore, Sponge is not initialized */
        }
        CapabilitySvc.notifyAboutEnable(plugin.isSystemEnabled());
    }
}
