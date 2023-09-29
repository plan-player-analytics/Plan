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

import com.djrapitops.plan.processing.Processing;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SchedulerSvc implements SchedulerService {

    private static final boolean SINGLE_THREADED = true;

    private final Processing processing;

    @Inject
    public SchedulerSvc(Processing processing) {
        this.processing = processing;
    }

    @Override
    public void runAsync(Runnable runnable) {
        processing.submitNonCritical(runnable, SINGLE_THREADED);
    }

    public void register() {
        Holder.set(this);
    }
}
