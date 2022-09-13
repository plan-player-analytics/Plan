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
package net.playeranalytics.plan.utilities.logging;

import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plan.PlanStandalone;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author AuroraLS3
 */
@Singleton
public class StandaloneErrorLogger implements ErrorLogger {

    private final PluginLogger logger;

    @Inject
    public StandaloneErrorLogger(PluginLogger logger) {
        this.logger = logger;
        // TODO Extract file logging properties of PluginErrorLogger without PlanPlugin as dependency.
    }

    @Override
    public void critical(Throwable throwable, ErrorContext context) {
        error(throwable, context);
        PlanStandalone.shutdown(1);
    }

    @Override
    public void error(Throwable throwable, ErrorContext context) {
        logger.error("", throwable);
    }

    @Override
    public void warn(Throwable throwable, ErrorContext context) {
        logger.warn("", throwable);
    }
}
