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
package com.djrapitops.plan.db;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.db.access.queries.ServerAggregateQueries;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.db.access.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.db.access.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.db.sql.tables.ServerTable;
import com.djrapitops.plan.system.info.server.Server;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.OptionalAssert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SQLiteTest extends CommonDBTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        handleSetup("SQLite");
    }

    @Test
    public void testSQLiteGetConfigName() {
        assertEquals("sqlite", db.getType().getConfigName());
    }

    @Test
    public void testSQLiteGetName() {
        assertEquals("SQLite", db.getType().getName());
    }

    @Test
    public void testServerTableBungeeSave() throws DBInitException {
        ServerTable serverTable = db.getServerTable();

        Optional<Server> bungeeInfo = db.query(ServerQueries.fetchProxyServerInformation());
        assertFalse(bungeeInfo.isPresent());

        UUID bungeeUUID = UUID.randomUUID();
        Server bungeeCord = new Server(-1, bungeeUUID, "BungeeCord", "Random:1234", 20);
        db.executeTransaction(new StoreServerInformationTransaction(bungeeCord));

        commitTest();

        bungeeCord.setId(2);

        bungeeInfo = db.query(ServerQueries.fetchProxyServerInformation());
        assertTrue(bungeeInfo.isPresent());
        assertEquals(bungeeCord, bungeeInfo.get());

        Optional<Server> found = db.query(ServerQueries.fetchServerMatchingIdentifier(bungeeUUID));
        OptionalAssert.equals(2, found.map(Server::getId));
    }

    @Test
    public void testServerTableBungee() throws DBInitException {
        testServerTableBungeeSave();

        Map<UUID, Server> serverInformation = db.query(ServerQueries.fetchPlanServerInformation());

        assertEquals(1, serverInformation.values().stream().filter(Server::isNotProxy).count());
        assertEquals(1, serverInformation.values().stream().filter(Server::isProxy).count());
    }

    @Test
    public void networkGeolocationsAreCountedAppropriately() {
        UUID firstUuid = UUID.randomUUID();
        UUID secondUuid = UUID.randomUUID();
        UUID thirdUuid = UUID.randomUUID();

        db.executeTransaction(new PlayerRegisterTransaction(firstUuid, () -> 0L, ""));
        db.executeTransaction(new PlayerRegisterTransaction(secondUuid, () -> 0L, ""));
        db.executeTransaction(new PlayerRegisterTransaction(thirdUuid, () -> 0L, ""));

        saveGeoInfo(firstUuid, new GeoInfo("-", "Norway", 0, "3"));
        saveGeoInfo(firstUuid, new GeoInfo("-", "Finland", 5, "3"));
        saveGeoInfo(secondUuid, new GeoInfo("-", "Sweden", 0, "3"));
        saveGeoInfo(thirdUuid, new GeoInfo("-", "Denmark", 0, "3"));

        Map<String, Integer> got = db.query(ServerAggregateQueries.networkGeolocationCounts());

        Map<String, Integer> expected = new HashMap<>();
        // first user has a more recent connection from Finland so their country should be counted as Finland.
        expected.put("Finland", 1);
        expected.put("Sweden", 1);
        expected.put("Denmark", 1);

        assertEquals(expected, got);
    }
}
