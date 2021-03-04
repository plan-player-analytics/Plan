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

import com.djrapitops.plan.DaggerPlanSpongeComponent;
import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.PlanSpongeComponent;
import com.djrapitops.plan.PlanSystem;

import java.nio.file.Path;

/**
 * Test utility for creating a dagger PlanComponent using a mocked PlanSponge.
 *
 * @author AuroraLS3
 */
public class SpongeMockComponent {

    private final Path tempDir;

    private PlanSponge planMock;
    private PlanSpongeComponent component;

    public SpongeMockComponent(Path tempDir) {
        this.tempDir = tempDir;
    }

    public PlanSponge getPlanMock() throws Exception {
        if (planMock == null) {
            planMock = PlanSpongeMocker.setUp()
                    .withDataFolder(tempDir.toFile())
                    .withResourceFetchingFromJar()
                    .withGame()
                    .getPlanMock();
        }
        return planMock;
    }

    public PlanSystem getPlanSystem() throws Exception {
        if (component == null) {
            component = DaggerPlanSpongeComponent.builder().plan(getPlanMock()).build();
        }
        return component.system();
    }
}