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
import com.djrapitops.plan.commands.use.ColorScheme;
import org.mockito.Mockito;

import java.io.File;

import static org.mockito.Mockito.doReturn;

/**
 * Mocking Utility for Bukkit version of Plan.
 *
 * @author AuroraLS3
 */
public class PlanPluginMocker {

    private PlanPlugin planMock;

    private PlanPluginMocker() {
    }

    public static PlanPluginMocker setUp() {
        return new PlanPluginMocker().mockPlugin();
    }

    private PlanPluginMocker mockPlugin() {
        planMock = Mockito.mock(PlanPlugin.class);

        doReturn(new ColorScheme("§1", "§2", "§3")).when(planMock).getColorScheme();

        return this;
    }

    public PlanPluginMocker withDataFolder(File tempFolder) {
        doReturn(tempFolder).when(planMock).getDataFolder();
        return this;
    }

    public PlanPluginMocker withLogging() {
        return this;
    }

    public PlanPlugin getPlanMock() {
        return planMock;
    }
}
