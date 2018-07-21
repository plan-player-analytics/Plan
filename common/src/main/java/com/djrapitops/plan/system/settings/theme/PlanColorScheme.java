/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.theme;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;

/**
 * ColorScheme that uses values in config settings specific to Plan or PlanBungee.
 *
 * @author Rsl1122
 */
public class PlanColorScheme extends ColorScheme {


    public PlanColorScheme(String... colors) {
        super(colors);
    }

    public static ColorScheme create() {
        try {
            String main = "§" + Settings.COLOR_MAIN.toString().charAt(1);
            String secondary = "§" + Settings.COLOR_SEC.toString().charAt(1);
            String tertiary = "§" + Settings.COLOR_TER.toString().charAt(1);

            return new ColorScheme(main, secondary, tertiary);
        } catch (Exception e) {
            Log.infoColor("§cCustomization, Chat colors set-up wrong, using defaults.");
            return new ColorScheme("§2", "§7", "§f");
        }
    }
}
