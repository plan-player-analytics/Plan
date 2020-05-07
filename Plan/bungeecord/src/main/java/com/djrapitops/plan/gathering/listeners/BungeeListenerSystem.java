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

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.events.PlanBungeeEnableEvent;
import com.djrapitops.plan.capability.CapabilitySvc;
import com.djrapitops.plan.gathering.listeners.bungee.PlayerOnlineListener;

import javax.inject.Inject;

public class BungeeListenerSystem extends ListenerSystem {

    private final PlanBungee plugin;
    private PlayerOnlineListener playerOnlineListener;

    @Inject
    public BungeeListenerSystem(PlanBungee plugin, PlayerOnlineListener playerOnlineListener) {
        this.plugin = plugin;
        this.playerOnlineListener = playerOnlineListener;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(playerOnlineListener);
    }

    @Override
    protected void unregisterListeners() {
        plugin.getProxy().getPluginManager().unregisterListeners(plugin);
    }

    @Override
    public void callEnableEvent(PlanPlugin plugin) {
        boolean isEnabled = plugin.isSystemEnabled();
        PlanBungeeEnableEvent event = new PlanBungeeEnableEvent(isEnabled);
        ((PlanBungee) plugin).getProxy().getPluginManager().callEvent(event);
        CapabilitySvc.notifyAboutEnable(isEnabled);
    }
}
