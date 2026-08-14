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
import net.minecraft.server.dedicated.DedicatedServer;
import net.playeranalytics.plan.DaggerPlanNeoForgeComponent;
import net.playeranalytics.plan.PlanNeoForgeComponent;
import net.playeranalytics.plan.identification.properties.NeoForgeServerProperties;
import org.mockito.Mockito;

import java.nio.file.Path;

import static org.mockito.Mockito.doReturn;

/**
 * Test utility for creating a dagger PlanComponent using a mocked Plan on NeoForge.
 */
public class NeoForgeMockComponent {

    private final Path tempDir;

    private PlanPlugin planMock;
    private PlanNeoForgeComponent component;

    public NeoForgeMockComponent(Path tempDir) {
        this.tempDir = tempDir;
        SQLDB.setDownloadDriver(false);
    }

    public PlanPlugin getPlanMock() {
        if (planMock == null) {
            planMock = PlanNeoForgeMocker.setUp()
                    .withDataFolder(tempDir.resolve("data").toFile())
                    .getPlanMock();
        }
        return planMock;
    }

    public PlanSystem getPlanSystem() {
        if (component == null) {
            PlanPlugin planMock = getPlanMock();
            component = DaggerPlanNeoForgeComponent.builder()
                    .plan(planMock)
                    .abstractionLayer(new TestPlatformAbstractionLayer(planMock))
                    .server(mockServer())
                    .serverProperties(mockServerProperties())
                    .build();
        }
        return component.system();
    }

    private DedicatedServer mockServer() {
        DedicatedServer serverMock = Mockito.mock(DedicatedServer.class);
        doReturn("").when(serverMock).getServerIp();
        doReturn(25565).when(serverMock).getPort();
        doReturn("26.2").when(serverMock).getServerVersion();
        return serverMock;
    }

    private NeoForgeServerProperties mockServerProperties() {
        NeoForgeServerProperties propertiesMock = Mockito.mock(NeoForgeServerProperties.class);
        doReturn("").when(propertiesMock).getIp();
        return propertiesMock;
    }
}
