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
package utilities.mocks;

import com.djrapitops.plan.DaggerPlanBukkitComponent;
import com.djrapitops.plan.PlanBukkitComponent;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.storage.database.SQLDB;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.Mockito;
import utilities.TestConstants;

import java.nio.file.Path;

import static org.mockito.Mockito.doReturn;

/**
 * Test utility for creating a dagger PlanComponent using a mocked Plan.
 *
 * @author AuroraLS3
 */
public class BukkitMockComponent {

    private final Path tempDir;

    private PlanPlugin planMock;
    private PlanBukkitComponent component;

    public BukkitMockComponent(Path tempDir) {
        this.tempDir = tempDir;
        SQLDB.setDownloadDriver(false);
    }

    public PlanPlugin getPlanMock() {
        if (planMock == null) {
            planMock = PlanPluginMocker.setUp()
                    .withDataFolder(tempDir.resolve("data").toFile())
                    .getPlanMock();
        }
        return planMock;
    }

    public PlanSystem getPlanSystem() {
        if (component == null) {
            PlanPlugin planMock = getPlanMock();
            component = DaggerPlanBukkitComponent.builder()
                    .plan(planMock)
                    .abstractionLayer(new TestPlatformAbstractionLayer(planMock))
                    .server(mockServer())
                    .build();
        }
        return component.system();
    }

    private Server mockServer() {
        Server serverMock = Mockito.mock(Server.class);
        doReturn("").when(serverMock).getIp();
        doReturn("Bukkit").when(serverMock).getName();
        doReturn(25565).when(serverMock).getPort();
        doReturn("1.12.2").when(serverMock).getVersion();
        doReturn("32423").when(serverMock).getBukkitVersion();
        doReturn(TestConstants.SERVER_MAX_PLAYERS).when(serverMock).getMaxPlayers();

        ConsoleCommandSender sender = Mockito.mock(ConsoleCommandSender.class);
        doReturn(sender).when(serverMock).getConsoleSender();

        BukkitScheduler bukkitScheduler = Mockito.mock(BukkitScheduler.class);
        doReturn(bukkitScheduler).when(serverMock).getScheduler();
        return serverMock;
    }
}