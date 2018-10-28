/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.export;

import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.file.export.HtmlExport;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * System in charge of exporting html.
 *
 * @author Rsl1122
 */
@Singleton
public class ExportSystem implements SubSystem {

    private final PlanConfig config;
    private final Processing processing;
    private final HtmlExport htmlExport;

    @Inject
    public ExportSystem(
            PlanConfig config,
            Processing processing,
            HtmlExport htmlExport
    ) {
        this.config = config;
        this.processing = processing;
        this.htmlExport = htmlExport;
    }

    @Override
    public void enable() {
        if (config.isTrue(Settings.ANALYSIS_EXPORT)) {
            processing.submitNonCritical(htmlExport);
        }
    }

    @Override
    public void disable() {

    }
}