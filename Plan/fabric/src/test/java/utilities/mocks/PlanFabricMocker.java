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

import com.djrapitops.plan.commands.use.ColorScheme;
import net.playeranalytics.plan.PlanFabric;
import org.mockito.Mockito;

import java.io.File;

import static org.mockito.Mockito.doReturn;

/**
 * Mocking utility for Fabric version of Plan.
 *
 * @author AuroraLS3
 * @author Kopo942
 */
public class PlanFabricMocker extends Mocker {

    private PlanFabric planMock;

    private PlanFabricMocker() {
    }

    public static PlanFabricMocker setUp() {
        return new PlanFabricMocker().mockPlugin();
    }

    private PlanFabricMocker mockPlugin() {
        planMock = Mockito.mock(PlanFabric.class);
        super.planMock = planMock;

        doReturn(new ColorScheme("§1", "§2", "§3")).when(planMock).getColorScheme();

        return this;
    }

    public PlanFabricMocker withDataFolder(File tempFolder) {
        doReturn(tempFolder).when(planMock).getDataFolder();
        return this;
    }

    PlanFabric getPlanMock() {
        return planMock;
    }
}
