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
package com.djrapitops.plan.settings.theme;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;

/**
 * ColorScheme that uses values in config settings specific to Plan or PlanBungee.
 *
 * @author AuroraLS3
 */
public class PlanColorScheme extends ColorScheme {

    private PlanColorScheme(String... colors) {
        super(colors);
    }

    public static PlanColorScheme create(PlanConfig config, PluginLogger logger) {
        try {
            String main = "§" + config.get(DisplaySettings.CMD_COLOR_MAIN).charAt(1);
            String secondary = "§" + config.get(DisplaySettings.CMD_COLOR_SECONDARY).charAt(1);
            String tertiary = "§" + config.get(DisplaySettings.CMD_COLOR_TERTIARY).charAt(1);

            return new PlanColorScheme(main, secondary, tertiary);
        } catch (Exception e) {
            logger.log(L.INFO_COLOR, "§cCustomization, Chat colors set-up wrong, using defaults.");
            return new PlanColorScheme("§2", "§7", "§f");
        }
    }

}
