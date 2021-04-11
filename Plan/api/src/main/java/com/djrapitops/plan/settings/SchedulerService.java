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
 * Service for registering async tasks as Plan.
 * <p>
 * This is an utility service for implementing async scheduling for platforms that don't have
 * static access to the task registration.
 */
public interface SchedulerService {

    /**
     * Obtain instance of ListenerService.
     *
     * @return QueryService implementation.
     * @throws NoClassDefFoundError  If Plan is not installed and this class can not be found or if older Plan version is installed.
     * @throws IllegalStateException If Plan is installed, but not enabled.
     */
    static SchedulerService getInstance() {
        return Optional.ofNullable(SchedulerService.Holder.service.get())
                .orElseThrow(() -> new IllegalStateException("ListenerService has not been initialised yet."));
    }

    /**
     * Attempts to register an async task to the platform as Plan.
     * <p>
     * This is an utility method for implementing tasks for platforms that don't have
     * static access to the task registration.
     *
     * @param runnable runnable object.
     */
    void runAsync(Runnable runnable);

    class Holder {
        static final AtomicReference<SchedulerService> service = new AtomicReference<>();

        private Holder() {
            /* Static variable holder */
        }

        static void set(SchedulerService service) {
            SchedulerService.Holder.service.set(service);
        }
    }
}
