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

import com.djrapitops.plan.exceptions.ExportException;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.utilities.java.ThrowingConsumer;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

public class ExportTask extends AbsRunnable {

    private final Exporter exporter;
    private final ThrowingConsumer<Exporter, ExportException> exportAction;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    public ExportTask(
            Exporter exporter,
            ThrowingConsumer<Exporter, ExportException> exportAction,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.exporter = exporter;
        this.exportAction = exportAction;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        try {
            exportAction.accept(exporter);
        } catch (ExportException e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        } catch (DBOpException dbException) {
            handleDBException(dbException);
        } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
            logger.error("Export Task Disabled due to error, reload Plan to re-enable.");
            errorHandler.log(L.ERROR, this.getClass(), e);
            cancel();
        }
    }

    private void handleDBException(DBOpException dbException) {
        logger.error("Export Task Disabled due to database error, reload Plan to re-enable.");
        if (dbException.getMessage().contains("closed")) {
            logger.warn("(Error was caused by database closing, so this error can possibly be ignored.)");
        } else {
            errorHandler.log(L.ERROR, this.getClass(), dbException);
        }
        cancel();
    }
}
