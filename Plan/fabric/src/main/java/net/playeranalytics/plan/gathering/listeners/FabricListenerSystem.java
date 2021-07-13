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

import javax.inject.Inject;

/**
 * Listener system for the Fabric platform.
 * @author Kopo942
 */
public class FabricListenerSystem extends ListenerSystem {

    @Inject
    public FabricListenerSystem() {

    }
    @Override
    protected void registerListeners() {

    }

    @Override
    protected void unregisterListeners() {

    }

    @Override
    public void callEnableEvent(PlanPlugin plugin) {

    }
}
