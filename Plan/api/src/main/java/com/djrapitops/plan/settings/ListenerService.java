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
package com.djrapitops.plan.settings;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service for registering listeners as Plan.
 * <p>
 * This is an utility service for implementing listeners for platforms that don't have
 * static access to the listener registration.
 */
public interface ListenerService {

    /**
     * Obtain instance of ListenerService.
     *
     * @return QueryService implementation.
     * @throws NoClassDefFoundError  If Plan is not installed and this class can not be found or if older Plan version is installed.
     * @throws IllegalStateException If Plan is installed, but not enabled.
     */
    static ListenerService getInstance() {
        return Optional.ofNullable(ListenerService.Holder.service.get())
                .orElseThrow(() -> new IllegalStateException("ListenerService has not been initialised yet."));
    }

    /**
     * Attempts to register an event listener to the platform as Plan.
     * <p>
     * This is an utility method for implementing listeners for platforms that don't have
     * static access to the listener registration.
     * <p>
     * The listener needs to fulfill the qualities required by the platform.
     *
     * @param listener listener object.
     */
    void registerListenerForPlan(Object listener);

    /**
     * Singleton holder for listeners.
     */
    class Holder {
        static final AtomicReference<ListenerService> service = new AtomicReference<>();

        private Holder() {
            /* Static variable holder */
        }

        static void set(ListenerService service) {
            ListenerService.Holder.service.set(service);
        }
    }
}
