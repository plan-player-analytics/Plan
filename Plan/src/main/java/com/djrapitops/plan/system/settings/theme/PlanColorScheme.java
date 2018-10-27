/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.theme;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;

/**
 * ColorScheme that uses values in config settings specific to Plan or PlanBungee.
 *
 * @author Rsl1122
 */
public class PlanColorScheme extends ColorScheme {

    private PlanColorScheme(String... colors) {
        super(colors);
    }

    public static PlanColorScheme create(PlanConfig config, PluginLogger logger) {
        try {
            String main = "§" + config.getString(Settings.COLOR_MAIN).charAt(1);
            String secondary = "§" + config.getString(Settings.COLOR_SEC).charAt(1);
            String tertiary = "§" + config.getString(Settings.COLOR_TER).charAt(1);

            return new PlanColorScheme(main, secondary, tertiary);
        } catch (Exception e) {
            logger.log(L.INFO_COLOR, "§cCustomization, Chat colors set-up wrong, using defaults.");
            return new PlanColorScheme("§2", "§7", "§f");
        }
    }

}
