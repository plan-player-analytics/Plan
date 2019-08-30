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
package com.djrapitops.plan.system.delivery.upkeep;

import com.djrapitops.plan.system.delivery.export.HtmlExport;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PeriodicAnalysisTask extends AbsRunnable {

    private final HtmlExport htmlExport;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    public PeriodicAnalysisTask(
            HtmlExport htmlExport,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.htmlExport = htmlExport;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        try {
            htmlExport.exportAvailableServerPages();
        } catch (IllegalStateException ignore) {
            /* Plugin was reloading */
        } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
            logger.error("Periodic Analysis Task Disabled due to error, reload Plan to re-enable.");
            errorHandler.log(L.ERROR, this.getClass(), e);
            cancel();
        }
    }
}
