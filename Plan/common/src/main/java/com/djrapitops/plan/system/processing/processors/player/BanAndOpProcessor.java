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

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Updates ban and OP status of the player to the database.
 *
 * @author Rsl1122
 */
public class BanAndOpProcessor implements Runnable {

    private final UUID uuid;
    private final Supplier<Boolean> banned;
    private final boolean op;

    private final Database database;

    BanAndOpProcessor(
            UUID uuid, Supplier<Boolean> banned, boolean op,
            Database database
    ) {
        this.uuid = uuid;
        this.banned = banned;
        this.op = op;
        this.database = database;
    }

    @Override
    public void run() {
        SaveOperations save = database.save();
        save.banStatus(uuid, banned.get());
        save.opStatus(uuid, op);
    }
}
