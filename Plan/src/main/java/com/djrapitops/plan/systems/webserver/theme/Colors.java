/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.theme;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Settings;

/**
 * Enum class used for getting the Html colors that match the config settings.
 *
 * @author Rsl1122
 */
public enum Colors {

    MAIN(0, Settings.THEME_COLOR_MAIN),
    MAIN_DARK(1, Settings.THEME_COLOR_MAIN_DARK),
    SECONDARY(2, Settings.THEME_COLOR_SECONDARY),
    SECONDARY_DARK(3, Settings.THEME_COLOR_SECONDARY_DARK),
    TERTIARY(4, Settings.THEME_COLOR_TERTIARY),
    TABLE_LIGHT(5, Settings.THEME_COLOR_TABLE_LIGHT),
    TABLE_DARK(6, Settings.THEME_COLOR_TABLE_DARK),
    FONT_LIGHT(7, Settings.THEME_FONT_COLOR_LIGHT),
    FONT_DARK(8, Settings.THEME_FONT_COLOR_DARK),
    TPS_HIGH(9, Settings.THEME_GRAPH_TPS_HIGH),
    TPS_MED(10, Settings.THEME_GRAPH_TPS_MED),
    TPS_LOW(11, Settings.THEME_GRAPH_TPS_LOW),
    PLAYERS_ONLINE(12, Settings.THEME_GRAPH_PLAYERS_ONLINE),
    CPU(13, Settings.THEME_GRAPH_CPU),
    RAM(14, Settings.THEME_GRAPH_RAM),
    CHUNKS(15, Settings.THEME_GRAPH_CHUNKS),
    ENTITIES(16, Settings.THEME_GRAPH_ENTITIES),
    PUNCHCARD(17, Settings.THEME_GRAPH_PUNCHCARD),
    BACKGROUND(18, Settings.THEME_COLOR_BACKGROUND);

    private final int id;
    private final Settings setting;

    Colors(int themeId, Settings setting) {
        id = themeId;
        this.setting = setting;
    }

    public String getColor() {
        String settingValue = setting.toString();
        try {
            if ("base".equalsIgnoreCase(settingValue)) {
                return "#" + Theme.valueOf(Settings.THEME_BASE.toString().toUpperCase()).getColor(id);
            } else if ('#' == settingValue.charAt(0)) {
                return settingValue;
            } else {
                for (Theme t : Theme.values()) {
                    if (t.name().equalsIgnoreCase(settingValue)) {
                        return "#" + t.getColor(id);
                    }
                }
            }
        } catch (Exception | NoSuchFieldError e) {
            Log.error("Something went wrong with getting color " + id + " for theme: " + settingValue);
        }
        return Theme.DEFAULT.getColor(id);
    }

    public int getId() {
        return id;
    }
}