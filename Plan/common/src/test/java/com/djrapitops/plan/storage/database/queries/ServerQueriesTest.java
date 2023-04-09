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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.SetServerAsUninstalledTransaction;
import org.junit.jupiter.api.Test;
import utilities.OptionalAssert;
import utilities.TestConstants;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public interface ServerQueriesTest extends DatabaseTestPreparer {

    @Test
    default void uninstallingServerStopsItFromBeingReturnedInServerQuery() {
        db().executeTransaction(new SetServerAsUninstalledTransaction(serverUUID()));

        Optional<Server> found = db().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID()));
        assertFalse(found.isPresent());
    }

    @Test
    default void uninstallingServerStopsItFromBeingReturnedInServersQuery() {
        db().executeTransaction(new SetServerAsUninstalledTransaction(serverUUID()));

        Collection<Server> found = db().query(ServerQueries.fetchPlanServerInformationCollection());
        assertTrue(found.isEmpty());
    }

    @Test
    default void bungeeInformationIsStored() {
        List<Server> proxies = db().query(ServerQueries.fetchProxyServers());
        assertTrue(proxies.isEmpty());

        ServerUUID bungeeUUID = ServerUUID.randomUUID();
        Server bungeeCord = new Server(bungeeUUID, "BungeeCord", "Random:1234", TestConstants.VERSION);
        bungeeCord.setProxy(true);
        db().executeTransaction(new StoreServerInformationTransaction(bungeeCord));

        forcePersistenceCheck();

        bungeeCord.setId(2);

        proxies = db().query(ServerQueries.fetchProxyServers());
        assertFalse(proxies.isEmpty());
        assertEquals(List.of(bungeeCord), proxies);

        Optional<Server> found = db().query(ServerQueries.fetchServerMatchingIdentifier(bungeeUUID));
        OptionalAssert.equals(bungeeCord.getWebAddress(), found.map(Server::getWebAddress));
    }

    @Test
    default void proxyIsDetected() {
        bungeeInformationIsStored();

        Map<ServerUUID, Server> serverInformation = db().query(ServerQueries.fetchPlanServerInformation());

        assertEquals(1, serverInformation.values().stream().filter(Server::isNotProxy).count());
        assertEquals(1, serverInformation.values().stream().filter(Server::isProxy).count());
    }

    @Test
    default void removeEverythingRemovesServers() {
        bungeeInformationIsStored();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(ServerQueries.fetchPlanServerInformation()).isEmpty());
    }
}
