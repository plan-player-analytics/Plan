/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.settings.theme;

import com.djrapitops.plan.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;

/**
 * ColorScheme that uses values in config settings specific to Plan or PlanBungee.
 *
 * @author Rsl1122
 */
public class PlanColorScheme extends ColorScheme {

    private PlanColorScheme(String... colors) {
        super(colors);
    }

    public static PlanColorScheme create() {
        try {
            String main = "§" + Settings.COLOR_MAIN.toString().charAt(1);
            String secondary = "§" + Settings.COLOR_SEC.toString().charAt(1);
            String tertiary = "§" + Settings.COLOR_TER.toString().charAt(1);

            return new PlanColorScheme(main, secondary, tertiary);
        } catch (Exception e) {
            Log.infoColor("§cCustomization, Chat colors set-up wrong, using defaults.");
            return new PlanColorScheme("§2", "§7", "§f");
        }
    }

}