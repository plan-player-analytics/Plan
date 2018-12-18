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
package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.api.events.PlanSpongeEnableEvent;
import com.djrapitops.plan.system.listeners.sponge.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;

import javax.inject.Inject;

public class SpongeListenerSystem extends ListenerSystem {

    private final PlanSponge plugin;

    private final SpongeAFKListener afkListener;
    private final SpongeChatListener chatListener;
    private final SpongeCommandListener commandListener;
    private final SpongeDeathListener deathListener;
    private final SpongeGMChangeListener gmChangeListener;
    private final SpongePlayerListener playerListener;
    private final SpongeWorldChangeListener worldChangeListener;

    @Inject
    public SpongeListenerSystem(PlanSponge plugin,
                                SpongeAFKListener afkListener,
                                SpongeChatListener chatListener,
                                SpongeCommandListener commandListener,
                                SpongeDeathListener deathListener,
                                SpongeGMChangeListener gmChangeListener,
                                SpongePlayerListener playerListener,
                                SpongeWorldChangeListener worldChangeListener
    ) {
        this.plugin = plugin;

        this.afkListener = afkListener;
        this.chatListener = chatListener;
        this.commandListener = commandListener;
        this.deathListener = deathListener;
        this.gmChangeListener = gmChangeListener;
        this.playerListener = playerListener;
        this.worldChangeListener = worldChangeListener;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(
                afkListener,
                chatListener,
                commandListener,
                deathListener,
                playerListener,
                gmChangeListener,
                worldChangeListener
        );
    }

    @Override
    protected void unregisterListeners() {
        Sponge.getEventManager().unregisterPluginListeners(plugin);
    }

    @Override
    public void callEnableEvent(PlanPlugin plugin) {
        Event event = new PlanSpongeEnableEvent((PlanSponge) plugin);
        Sponge.getEventManager().post(event);
    }
}
