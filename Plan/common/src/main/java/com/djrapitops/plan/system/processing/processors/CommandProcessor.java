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
package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;

/**
 * Updates Command usage amount in the database.
 *
 * @author Rsl1122
 */
public class CommandProcessor implements CriticalRunnable {

    private final String command;

    private final Database database;

    CommandProcessor(String command, Database database) {
        this.command = command;
        this.database = database;
    }

    @Override
    public void run() {
        database.save().commandUsed(command);
    }
}
