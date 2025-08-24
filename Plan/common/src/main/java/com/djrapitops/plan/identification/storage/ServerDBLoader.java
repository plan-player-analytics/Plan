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
package com.djrapitops.plan.identification.storage;

import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Singleton
public class ServerDBLoader implements ServerLoader {

    private final DBSystem dbSystem;

    @Inject
    public ServerDBLoader(
            DBSystem dbSystem
    ) {
        this.dbSystem = dbSystem;
    }

    @Override
    public Optional<Server> load(ServerUUID serverUUID) {
        try {
            if (serverUUID == null) {
                throw new EnableException("Attempted to load a server with null UUID (Old behavior that is no longer supported)");
            }

            return dbSystem.getDatabase().query(
                    ServerQueries.fetchServerMatchingIdentifier(serverUUID)
            );
        } catch (DBOpException e) {
            throw new EnableException("Failed to read Server information from Database: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Server server) {
        if (server == null) return;
        try {
            dbSystem.getDatabase().executeTransaction(
                    new StoreServerInformationTransaction(server)
            ).get(); // Wait until transaction has completed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new EnableException("Failed to save server information to database: " + e.getMessage(), e);
        }
    }
}
