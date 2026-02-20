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

import com.djrapitops.plan.DaggerPlanNukkitComponent;
import com.djrapitops.plan.PlanNukkit;
import com.djrapitops.plan.PlanNukkitComponent;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.storage.database.SQLDB;

import java.nio.file.Path;

/**
 * Test utility for creating a dagger PlanComponent using a mocked Plan.
 *
 * @author AuroraLS3
 */
public class NukkitMockComponent {

    private final Path tempDir;

    private PlanNukkit planMock;
    private PlanNukkitComponent component;

    public NukkitMockComponent(Path tempDir) {
        this.tempDir = tempDir;
        SQLDB.setDownloadDriver(false);
    }

    public PlanNukkit getPlanMock() {
        if (planMock == null) {
            planMock = PlanNukkitMocker.setUp()
                    .withDataFolder(tempDir.toFile())
                    .withPluginDescription()
                    .withServer()
                    .getPlanMock();
        }
        return planMock;
    }

    public PlanSystem getPlanSystem() {
        if (component == null) {
            PlanNukkit planMock = getPlanMock();
            component = DaggerPlanNukkitComponent.builder()
                    .plan(planMock)
                    .abstractionLayer(new TestPlatformAbstractionLayer(this.planMock))
                    .build();
        }
        return component.system();
    }
}