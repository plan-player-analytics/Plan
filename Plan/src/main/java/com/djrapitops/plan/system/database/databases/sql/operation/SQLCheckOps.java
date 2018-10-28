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
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.operation.CheckOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.util.UUID;

public class SQLCheckOps extends SQLOps implements CheckOperations {

    public SQLCheckOps(SQLDB db) {
        super(db);
    }

    @Override
    public boolean isPlayerRegistered(UUID player) {
        return usersTable.isRegistered(player);
    }

    @Override
    public boolean isPlayerRegistered(UUID player, UUID server) {
        return userInfoTable.isRegistered(player, server);
    }

    @Override
    public boolean isPlayerRegisteredOnThisServer(UUID player) {
        return userInfoTable.isRegisteredOnThisServer(player);
    }

    @Override
    public boolean doesWebUserExists(String username) {
        return securityTable.userExists(username);
    }

    @Override
    public boolean isServerInDatabase(UUID serverUUID) {
        return serverTable.getServerID(serverUUID).isPresent();
    }
}
