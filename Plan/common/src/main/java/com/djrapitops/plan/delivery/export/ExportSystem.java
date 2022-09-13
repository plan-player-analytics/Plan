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
package com.djrapitops.plan.delivery.export;

import com.djrapitops.plan.SubSystem;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * System in charge of exporting html.
 *
 * @author AuroraLS3
 */
@Singleton
public class ExportSystem implements SubSystem {

    private final Exporter exporter;
    private final ExportScheduler exportScheduler;
    private final RunnableFactory runnableFactory;

    @Inject
    public ExportSystem(
            Exporter exporter,
            ExportScheduler exportScheduler,
            RunnableFactory runnableFactory
    ) {
        this.exporter = exporter;
        this.exportScheduler = exportScheduler;
        this.runnableFactory = runnableFactory;
    }

    @Override
    public void enable() {
        runnableFactory.create(exportScheduler).runTaskAsynchronously();
    }

    @Override
    public void disable() {
        // Nothing to disable
    }

    public Exporter getExporter() {
        return exporter;
    }
}