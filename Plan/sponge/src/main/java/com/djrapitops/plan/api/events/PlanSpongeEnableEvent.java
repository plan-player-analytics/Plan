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

import com.djrapitops.plan.PlanSponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * Event that is called when Plan is enabled.
 * <p>
 * This includes, but might not be limited to:
 * - First time the plugin enables successfully
 * - Plan is reloaded
 * - Sponge-BungeeCord setup updates settings
 * - Plan is enabled after it was disabled
 * <p>
 * {@code event.isPlanSystemEnabled()} can be called to determine if the enable was successful.
 * It is not guaranteed that this event is called when the plugin fails to enable properly.
 *
 * @author AuroraLS3
 */
public class PlanSpongeEnableEvent extends AbstractEvent {

    private final PlanSponge plugin;
    private final boolean enabled;

    public PlanSpongeEnableEvent(PlanSponge plugin) {
        this.plugin = plugin;
        this.enabled = plugin.isSystemEnabled();
    }

    public boolean isPlanSystemEnabled() {
        return enabled;
    }

    @Override
    public Cause getCause() {
        return Cause.builder().append(plugin.getSystem()).build(EventContext.empty());
    }

    @Override
    public Object getSource() {
        return plugin.getSystem();
    }

    @Override
    public EventContext getContext() {
        return EventContext.empty();
    }
}
