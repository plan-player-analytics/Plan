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
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.CheckOperations;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Registers the user to the database and marks first session if the user has no actions.
 *
 * @author Rsl1122
 */
public class RegisterProcessor extends AbsRunnable {

    private final UUID uuid;
    private final Supplier<Long> registered;
    private final String name;
    private final Runnable[] afterProcess;

    private final Processing processing;
    private final Database database;

    RegisterProcessor(
            UUID uuid, Supplier<Long> registered, String name,
            Processing processing, Database database,
            Runnable... afterProcess
    ) {
        this.uuid = uuid;
        this.registered = registered;
        this.name = name;
        this.processing = processing;
        this.database = database;
        this.afterProcess = afterProcess;
    }

    @Override
    public void run() {
        Verify.nullCheck(uuid, () -> new IllegalStateException("UUID was null"));

        CheckOperations check = database.check();
        SaveOperations save = database.save();
        try {
            if (!check.isPlayerRegistered(uuid)) {
                save.registerNewUser(uuid, registered.get(), name);
            }
            if (!check.isPlayerRegisteredOnThisServer(uuid)) {
                save.registerNewUserOnThisServer(uuid, registered.get());
            }
        } finally {
            for (Runnable runnable : afterProcess) {
                processing.submit(runnable);
            }
            cancel();
        }
    }
}
