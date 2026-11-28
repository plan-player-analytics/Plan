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
package com.djrapitops.plan.extension.implementation;

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.FlagsProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.statistic.Flags;

import java.util.UUID;

/**
 * @author AuroraLS3
 */
@PluginInfo(name = "ExamplePlugin", color = Color.AMBER)
public class ExampleExtension implements DataExtension {

    @FlagsProvider(text = "Player Flags", tableColor = Color.AMBER)
    public Flags playerFlags(UUID playerUUID) {
        return Flags.create()
                .put("Works", true)
                .put("Failing", false)
                .put("All good", null);
    }

    @FlagsProvider(text = "Server Flags", tableColor = Color.AMBER)
    public Flags serverFlags() {
        return Flags.create()
                .put("Works", true)
                .put("Failing", false)
                .put("All good", null);
    }
}
