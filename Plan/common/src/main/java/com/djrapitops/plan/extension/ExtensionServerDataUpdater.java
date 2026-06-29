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
package com.djrapitops.plan.extension;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * Task for updating {@link DataExtension} server values periodically.
 *
 * @author AuroraLS3
 */
@Singleton
public class ExtensionServerDataUpdater extends TaskSystem.Task {

    private final ExtensionSvc service;
    private final PlanConfig config;

    @Inject
    public ExtensionServerDataUpdater(
            ExtensionSvc service,
            PlanConfig config
    ) {
        this.service = service;
        this.config = config;
    }

    @Override
    public void run() {
        service.updateServerValues(CallEvents.SERVER_PERIODICAL);
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        long period = TimeAmount.toTicks(config.get(TimeSettings.EXTENSION_SERVER_DATA_REFRESH_PERIOD), TimeUnit.MILLISECONDS);
        long delay = TimeAmount.toTicks(30, TimeUnit.SECONDS);
        runnableFactory.create(this).runTaskTimerAsynchronously(delay, period);
    }
}