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
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanBukkitComponent;
import com.djrapitops.plan.PlanSystem;

import java.nio.file.Path;

/**
 * Test utility for creating a dagger PlanComponent using a mocked Plan.
 *
 * @author AuroraLS3
 */
public class BukkitMockComponent {

    private final Path tempDir;

    private Plan planMock;
    private PlanBukkitComponent component;

    public BukkitMockComponent(Path tempDir) {
        this.tempDir = tempDir;
    }

    public Plan getPlanMock() throws Exception {
        if (planMock == null) {
            planMock = PlanBukkitMocker.setUp()
                    .withDataFolder(tempDir.toFile())
                    .withPluginDescription()
                    .withResourceFetchingFromJar()
                    .withServer()
                    .getPlanMock();
        }
        return planMock;
    }

    public PlanSystem getPlanSystem() throws Exception {
        if (component == null) {
            component = DaggerPlanBukkitComponent.builder().plan(getPlanMock()).build();
        }
        return component.system();
    }
}