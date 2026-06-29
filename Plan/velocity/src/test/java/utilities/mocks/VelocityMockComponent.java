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

import com.djrapitops.plan.DaggerPlanVelocityComponent;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.PlanVelocityComponent;
import com.djrapitops.plan.storage.database.SQLDB;

import java.nio.file.Path;

/**
 * Test utility for creating a dagger PlanComponent using a mocked Plan.
 *
 * @author AuroraLS3
 */
public class VelocityMockComponent {

    private final Path tempDir;

    private PlanVelocity planMock;
    private PlanVelocityComponent component;

    public VelocityMockComponent(Path tempDir) {
        this.tempDir = tempDir;
        SQLDB.setDownloadDriver(false);
    }

    public PlanVelocity getPlanMock() {
        if (planMock == null) {
            planMock = PlanVelocityMocker.setUp()
                    .withDataFolder(tempDir.toFile())
                    .withProxy()
                    .getPlanMock();
        }
        return planMock;
    }

    public PlanSystem getPlanSystem() {
        if (component == null) {
            PlanVelocity planMock = getPlanMock();
            component = DaggerPlanVelocityComponent.builder()
                    .plan(planMock)
                    .abstractionLayer(new TestPlatformAbstractionLayer(this.planMock))
                    .build();
        }
        return component.system();
    }
}