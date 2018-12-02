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

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.PlanSystem;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import utilities.dagger.DaggerPlanPluginComponent;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.PlanPluginMocker;

public class PluginComponentMocker extends ExternalResource implements ComponentMocker {
    private final TemporaryFolder testFolder;

    private PlanPlugin planMock;
    private PlanPluginComponent component;

    public PluginComponentMocker(TemporaryFolder testFolder) {
        this.testFolder = testFolder;
    }

    @Override
    protected void before() throws Throwable {
        PlanPluginMocker mocker = PlanPluginMocker.setUp()
                .withDataFolder(testFolder.newFolder())
                .withResourceFetchingFromJar()
                .withLogging();
        planMock = mocker.getPlanMock();
        component = DaggerPlanPluginComponent.builder().plan(planMock).build();
    }

    public PlanPlugin getPlanMock() {
        return planMock;
    }

    public PlanSystem getPlanSystem() {
        return component.system();
    }
}
