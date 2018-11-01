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
package com.djrapitops.plan.system.database.databases;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.tables.ServerTable;
import com.djrapitops.plan.system.info.server.Server;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

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
    public void testH2GetConfigName() {
        assertEquals("sqlite", db.getType().getConfigName());
    }

    @Test
    public void testH2GetName() {
        assertEquals("SQLite", db.getType().getName());
    }

    @Test
    public void testServerTableBungeeSave() throws DBInitException {
        ServerTable serverTable = db.getServerTable();

        Optional<Server> bungeeInfo = serverTable.getBungeeInfo();
        assertFalse(bungeeInfo.isPresent());

        UUID bungeeUUID = UUID.randomUUID();
        Server bungeeCord = new Server(-1, bungeeUUID, "BungeeCord", "Random:1234", 20);
        serverTable.saveCurrentServerInfo(bungeeCord);

        commitTest();

        bungeeCord.setId(2);

        bungeeInfo = serverTable.getBungeeInfo();
        assertTrue(bungeeInfo.isPresent());
        assertEquals(bungeeCord, bungeeInfo.get());

        Optional<Integer> serverID = serverTable.getServerID(bungeeUUID);
        assertTrue(serverID.isPresent());
        assertEquals(2, (int) serverID.get());
    }

    @Test
    public void testServerTableBungee() throws DBInitException {
        testServerTableBungeeSave();
        ServerTable serverTable = db.getServerTable();

        Map<UUID, Server> bukkitServers = serverTable.getBukkitServers();
        assertEquals(1, bukkitServers.size());
    }
}
