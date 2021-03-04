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
package com.djrapitops.plan.api.events;

/**
 * Event that is called when Plan is enabled.
 * <p>
 * This includes, but might not be limited to:
 * - First time the plugin enables successfully
 * - Plan is reloaded
 * - Bukkit-BungeeCord setup updates settings
 * - Plan is enabled after it was disabled
 * <p>
 * {@code event.isPlanSystemEnabled()} can be called to determine if the enable was successful.
 * It is not guaranteed that this event is called when the plugin fails to enable properly.
 *
 * @author AuroraLS3
 */
public class PlanVelocityEnableEvent {

    private final boolean enabled;

    public PlanVelocityEnableEvent(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPlanSystemEnabled() {
        return enabled;
    }

}
