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
package com.djrapitops.plan;

import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import rules.BukkitComponentMocker;
import rules.BungeeComponentMocker;
import rules.ComponentMocker;
import utilities.RandomData;

import java.util.UUID;

/**
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BungeeBukkitConnectionTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ComponentMocker bukkitComponent = new BukkitComponentMocker(temporaryFolder);
    @ClassRule
    public static ComponentMocker bungeeComponent = new BungeeComponentMocker(temporaryFolder);

    private final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PlanSystem bukkitSystem;
    private PlanSystem bungeeSystem;

    @After
    public void tearDown() {
        System.out.println("------------------------------");
        System.out.println("Disable");
        System.out.println("------------------------------");
        if (bukkitSystem != null) {
            bukkitSystem.disable();
        }
        if (bungeeSystem != null) {
            bungeeSystem.disable();
        }
    }

    public void enable() throws Exception {
        bukkitSystem = bukkitComponent.getPlanSystem();
        bungeeSystem = bungeeComponent.getPlanSystem();

        bukkitSystem.getConfigSystem().getConfig().set(WebserverSettings.PORT, TEST_PORT_NUMBER);
        bungeeSystem.getConfigSystem().getConfig().set(WebserverSettings.PORT, 9250);

        DBSystem dbSystem = bungeeSystem.getDatabaseSystem();
        dbSystem.setActiveDatabase(dbSystem.getSqLiteFactory().usingDefaultFile());

        bukkitSystem.enable();
        bungeeSystem.enable();

        UUID bukkitUUID = bukkitSystem.getServerInfo().getServerUUID();
        UUID bungeeUUID = bungeeSystem.getServerInfo().getServerUUID();

        System.out.println("------------------------------");
        System.out.println("Enable Complete");
        System.out.println("Bukkit: " + bukkitUUID);
        System.out.println("Bungee: " + bungeeUUID);
        System.out.println("------------------------------");
    }

    @Test
    @Ignore("InfoRequestFactory not available via getters")
    public void testRequest() throws Exception {
        enable();

        System.out.println("Sending request");
//        bungeeSystem.getInfoSystem().getConnectionSystem().sendWideInfoRequest(new GenerateInspectPluginsTabRequest(infoSystem, infoRequestFactory, TestConstants.PLAYER_ONE_UUID));
    }
}
