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
import utilities.TestResources;

import java.io.File;

/**
 * Abstract Mocker for methods that can be used for both Bungee and Bukkit.
 *
 * @author AuroraLS3
 */
abstract class Mocker {

    PlanPlugin planMock;

    File getFile(String fileName) {
        // Read the resource from jar to a temporary file
        File file = new File(new File(planMock.getDataFolder(), "jar"), fileName);
        TestResources.copyResourceIntoFile(file, fileName);
        return file;
    }

}
