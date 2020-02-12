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
package com.djrapitops.plan.capability;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Singleton instance implementation for {@link CapabilityService}.
 * <p>
 * Only one instance exists per runtime in order to notify others when the plugin enables.
 *
 * @author Rsl1122
 */
public class CapabilitySvc implements CapabilityService {

    private final List<Consumer<Boolean>> enableListeners;

    private CapabilitySvc() {
        Holder.set(this);
        enableListeners = new ArrayList<>();
    }

    private static CapabilitySvc get() {
        if (Holder.service == null) {
            return new CapabilitySvc();
        }
        return (CapabilitySvc) Holder.service;
    }

    public static void initialize() {
        get();
    }

    public static void notifyAboutEnable(boolean isEnabled) {
        for (Consumer<Boolean> enableListener : get().enableListeners) {
            enableListener.accept(isEnabled);
        }
    }

    @Override
    public void registerEnableListener(Consumer<Boolean> enableListener) {
        enableListeners.add(enableListener);
    }
}