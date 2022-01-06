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

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.storage.database.SQLDB;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.playeranalytics.plan.DaggerPlanFabricComponent;
import net.playeranalytics.plan.PlanFabricComponent;
import net.playeranalytics.plan.identification.properties.FabricServerProperties;
import org.mockito.Mockito;

import java.nio.file.Path;

import static org.mockito.Mockito.doReturn;

/**
 * Test utility for creating a dagger PlanComponent using a mocked Plan.
 *
 * @author AuroraLS3
 * @author Kopo942
 */
public class FabricMockComponent {

    private final Path tempDir;

    private PlanPlugin planMock;
    private PlanFabricComponent component;

    public FabricMockComponent(Path tempDir) {
        this.tempDir = tempDir;
        SQLDB.setDownloadDriver(false);
    }

    public PlanPlugin getPlanMock() {
        if (planMock == null) {
            planMock = PlanFabricMocker.setUp()
                    .withDataFolder(tempDir.resolve("data").toFile())
                    .getPlanMock();
        }
        return planMock;
    }

    public PlanSystem getPlanSystem() {
        if (component == null) {
            PlanPlugin planMock = getPlanMock();
            component = DaggerPlanFabricComponent.builder()
                    .plan(planMock)
                    .abstractionLayer(new TestPlatformAbstractionLayer(planMock))
                    .server(mockServer())
                    .serverProperties(mockServerProperties())
                    .build();
        }
        return component.system();
    }

    private MinecraftDedicatedServer mockServer() {
        MinecraftDedicatedServer serverMock = Mockito.mock(MinecraftDedicatedServer.class);
        doReturn("").when(serverMock).getServerIp();
        doReturn(25565).when(serverMock).getPort();
        doReturn("1.17.1").when(serverMock).getVersion();
        return serverMock;
    }

    private FabricServerProperties mockServerProperties() {
        FabricServerProperties propertiesMock = Mockito.mock(FabricServerProperties.class);
        doReturn("").when(propertiesMock).getIp();
        return propertiesMock;
    }
}