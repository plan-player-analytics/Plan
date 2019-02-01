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
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.PlayerFetchQueries;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plan.system.processing.Processing;

import java.util.UUID;

/**
 * Processor that registers a new User for all servers to use as UUID - ID reference.
 *
 * @author Rsl1122
 */
public class ProxyRegisterProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final String name;
    private final long registered;
    private final Runnable[] afterProcess;

    private final Processing processing;
    private final Database database;

    ProxyRegisterProcessor(
            UUID uuid, String name, long registered,
            Processing processing,
            Database database,
            Runnable... afterProcess
    ) {
        this.uuid = uuid;
        this.name = name;
        this.registered = registered;
        this.processing = processing;
        this.database = database;
        this.afterProcess = afterProcess;
    }

    @Override
    public void run() {
        try {
            if (database.query(PlayerFetchQueries.isPlayerRegistered(uuid))) {
                return;
            }
            database.save().registerNewUser(uuid, registered, name);
        } finally {
            for (Runnable process : afterProcess) {
                processing.submit(process);
            }
        }
    }
}
