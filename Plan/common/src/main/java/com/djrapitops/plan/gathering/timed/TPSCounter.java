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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.gathering.SystemUsage;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

/**
 * Class responsible for calculating TPS every second.
 *
 * @author Rsl1122
 */
public abstract class TPSCounter extends AbsRunnable {

    protected final PluginLogger logger;
    protected final ErrorHandler errorHandler;

    private boolean diskErrored = false;

    public TPSCounter(
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        try {
            pulse();
        } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
            logger.error("TPS Count Task Disabled due to error, reload Plan to re-enable.");
            errorHandler.log(L.ERROR, this.getClass(), e);
            cancel();
        }
    }

    public abstract void pulse();

    protected long getFreeDiskSpace() {
        try {
            return SystemUsage.getFreeDiskSpace();
        } catch (SecurityException noPermission) {
            if (!diskErrored) {
                errorHandler.log(L.WARN, this.getClass(), noPermission);
            }
            diskErrored = true;
            return -1;
        }
    }
}
