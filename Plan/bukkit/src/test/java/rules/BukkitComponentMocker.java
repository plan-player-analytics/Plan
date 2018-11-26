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
package rules;

import com.djrapitops.plan.DaggerPlanBukkitComponent;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanBukkitComponent;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.PlanSystem;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import utilities.mocks.PlanBukkitMocker;

public class BukkitComponentMocker extends ExternalResource implements ComponentMocker {

    private final TemporaryFolder testFolder;

    private Plan planMock;
    private PlanBukkitComponent component;

    public BukkitComponentMocker(TemporaryFolder testFolder) {
        this.testFolder = testFolder;
    }

    @Override
    protected void before() throws Throwable {
        PlanBukkitMocker mocker = PlanBukkitMocker.setUp()
                .withDataFolder(testFolder.newFolder())
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        planMock = mocker.getPlanMock();
        component = DaggerPlanBukkitComponent.builder().plan(planMock).build();
    }

    public PlanPlugin getPlanMock() {
        return planMock;
    }

    public PlanSystem getPlanSystem() {
        return component.system();
    }
}
