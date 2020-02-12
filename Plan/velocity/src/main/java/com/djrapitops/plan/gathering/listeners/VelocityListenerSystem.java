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
import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.api.events.PlanVelocityEnableEvent;
import com.djrapitops.plan.capability.CapabilitySvc;
import com.djrapitops.plan.gathering.listeners.velocity.PlayerOnlineListener;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VelocityListenerSystem extends ListenerSystem {

    private final PlanVelocity plugin;

    private final PlayerOnlineListener playerOnlineListener;

    @Inject
    public VelocityListenerSystem(
            PlanVelocity plugin,
            PlayerOnlineListener playerOnlineListener
    ) {
        this.plugin = plugin;
        this.playerOnlineListener = playerOnlineListener;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(playerOnlineListener);
    }

    @Override
    protected void unregisterListeners() {
        plugin.getProxy().getEventManager().unregisterListeners(plugin);
    }

    @Override
    public void callEnableEvent(PlanPlugin plugin) {
        boolean isEnabled = plugin.isSystemEnabled();
        PlanVelocityEnableEvent event = new PlanVelocityEnableEvent(isEnabled);
        ((PlanVelocity) plugin).getProxy().getEventManager().fireAndForget(event);
        CapabilitySvc.notifyAboutEnable(isEnabled);
    }
}
