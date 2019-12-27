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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.storage.database.queries.objects.GeoInfoQueries;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.DBPreparer;
import utilities.OptionalAssert;
import utilities.RandomData;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Rsl1122
 */
@RunWith(JUnitPlatform.class)
@ExtendWith(MockitoExtension.class)
public class SQLiteTest implements DatabaseTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    private static PlanSystem system;
    private static Database database;

    @BeforeAll
    static void setupDatabase(@TempDir Path temp) throws Exception {
        system = new PluginMockComponent(temp).getPlanSystem();
        database = new DBPreparer(system, TEST_PORT_NUMBER).prepareSQLite()
                .orElseThrow(IllegalStateException::new);
    }

    @AfterAll
    static void disableSystem() {
        if (database != null) database.close();
        system.disable();
    }

    @Override
    public Database db() {
        return database;
    }

    @Override
    public UUID serverUUID() {
        return system.getServerInfo().getServerUUID();
    }

    @Override
    public PlanSystem system() {
        return system;
    }

    @Test
    void testServerTableBungeeSave() {
        Optional<Server> bungeeInfo = database.query(ServerQueries.fetchProxyServerInformation());
        assertFalse(bungeeInfo.isPresent());

        UUID bungeeUUID = UUID.randomUUID();
        Server bungeeCord = new Server(-1, bungeeUUID, "BungeeCord", "Random:1234", 20);
        database.executeTransaction(new StoreServerInformationTransaction(bungeeCord));

        commitTest();

        bungeeCord.setId(2);

        bungeeInfo = database.query(ServerQueries.fetchProxyServerInformation());
        assertTrue(bungeeInfo.isPresent());
        assertEquals(bungeeCord, bungeeInfo.get());

        Optional<Server> found = database.query(ServerQueries.fetchServerMatchingIdentifier(bungeeUUID));
        OptionalAssert.equals(2, found.map(Server::getId));
    }

    @Test
    void testServerTableBungee() {
        testServerTableBungeeSave();

        Map<UUID, Server> serverInformation = database.query(ServerQueries.fetchPlanServerInformation());

        assertEquals(1, serverInformation.values().stream().filter(Server::isNotProxy).count());
        assertEquals(1, serverInformation.values().stream().filter(Server::isProxy).count());
    }

    @Test
    void networkGeolocationsAreCountedAppropriately() {
        UUID firstUuid = UUID.randomUUID();
        UUID secondUuid = UUID.randomUUID();
        UUID thirdUuid = UUID.randomUUID();
        UUID fourthUuid = UUID.randomUUID();
        UUID fifthUuid = UUID.randomUUID();
        UUID sixthUuid = UUID.randomUUID();

        database.executeTransaction(new PlayerRegisterTransaction(firstUuid, () -> 0L, ""));
        database.executeTransaction(new PlayerRegisterTransaction(secondUuid, () -> 0L, ""));
        database.executeTransaction(new PlayerRegisterTransaction(thirdUuid, () -> 0L, ""));

        saveGeoInfo(firstUuid, new GeoInfo("Norway", 0));
        saveGeoInfo(firstUuid, new GeoInfo("Finland", 5));
        saveGeoInfo(secondUuid, new GeoInfo("Sweden", 0));
        saveGeoInfo(thirdUuid, new GeoInfo("Denmark", 0));
        saveGeoInfo(fourthUuid, new GeoInfo("Denmark", 0));
        saveGeoInfo(fifthUuid, new GeoInfo("Not Known", 0));
        saveGeoInfo(sixthUuid, new GeoInfo("Local Machine", 0));

        Map<String, Integer> got = database.query(GeoInfoQueries.networkGeolocationCounts());

        Map<String, Integer> expected = new HashMap<>();
        // first user has a more recent connection from Finland so their country should be counted as Finland.
        expected.put("Finland", 1);
        expected.put("Sweden", 1);
        expected.put("Not Known", 1);
        expected.put("Local Machine", 1);
        expected.put("Denmark", 2);

        assertEquals(expected, got);
    }
}
