/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.theme;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.settings.Settings;

/**
 * Enum that contains available themes.
 * <p>
 * Change config setting Theme.Base to select one.
 *
 * @author Rsl1122
 */
public enum Theme {

    // Main, Main-Dark, Secondary, Secondary-Dark, Tertiary, Table-Light, Table-Dark,
    // White, Black, TPSHigh, TPSMed, TPSLow, PlayersOnline
    // CPU, RAM, Chunks, Entities, PunchCard, Background
    DEFAULT("348e0f", "267f00", "5da341", "348e0f", "89c471", "eee", "e2e2e2",
            "fff", "000", "267F00", "e5cc12", "b74343", "1E90FF",
            "e0d264", "7dcc24", "b58310", "ac69ef", "222", "ddd"),
    GREYSCALE("222", "111", "424242", "212121", "9e9e9e", "eee", "e2e2e2",
            "fff", "000", "111", "616161", "9e9e9e", "bdbdbd",
            "bdbdbd", "9e9e9e", "bdbdbd", "9e9e9e", "222", "ddd"),
    BLAZE("ff8f00", "ff6100", "ffb300", "ffa000", "ffca28", "fff8e1", "ffecb3",
            "fff", "000", "ffa000", "ff8f00", "ff6f00", "ff8f00",
            "ffb300", "ff8f00", "ffb300", "ff8f00", "e65100", "ddd"),
    DARKRED("8A3324", "832A0D", "a54433", "954535", "826644", "eee", "e2e2e2",
            "fff", "000", "267F00", "e5cc12", "b74343", "CD853F",
            "BC8F8F", "CD853F", "C19A6B", "D2B48C", "222", "89524f"),
    BLUEGRAY("2f3f47", "293338", "37474f", "263238", "546e7a", "eee", "e2e2e2",
            "fff", "000", "78909c", "455a64", "546e7a", "78909c",
            "78909c", "546e7a", "78909c", "546e7a", "222", "ddd"),
    PURPLE("4527a0", "311b92", "5e35b1", "512da8", "7c4dff", "eee", "e2e2e2",
            "fff", "000", "9575cd", "7e57c2", "673ab7", "b39ddb",
            "651fff", "7c4dff", "9575cd", "b39ddb", "222", "ede7f6"),
    INDIGO("283593", "1a237e", "3949ab", "303f9f", "5c6bc0", "eee", "e2e2e2",
            "fff", "000", "7986cb", "5c6bc0", "3f51b5", "536dfe",
            "9fa8da", "5c6bc0", "3d5afe", "536dfe", "222", "e8eaf6"),
    PINK("c2185b", "ad1457", "e91e63", "d81b60", "ff80ab", "eee", "e2e2e2",
            "fff", "000", "f06292", "ec407a", "880e4f", "f06292",
            "f48fb1", "ec407a", "f48fb1", "f06292", "222", "fce4ec"),
    // TODO Tweak table colors
    NIGHT("212021", "494849", "333133", "6B686B", "514F51", "BCB8BC", "ccc",
            "fff", "000", "267F00", "e5cc12", "b74343", "1E90FF",
            "e0d264", "7dcc24", "b58310", "ac69ef", "ddd", "565556");

    private final String[] colors;

    Theme(String... colors) {
        int length = colors.length;
        if (length < Colors.values().length) {
            Log.error("Not All colors (" + length + ") were specified in the theme file: " + name());
            Log.error("If the theme is used it WILL CAUSE EXCEPTIONS.");
        }
        this.colors = colors;
    }

    public static String replaceColors(String resourceString) {
        String replaced = resourceString;
        for (Colors c : Colors.values()) {
            String color = c.getColor();
            if (!color.contains("url")) {
                replaced = replaced.replace("#" + Theme.DEFAULT.getColor(c.getId()), color);
            } else {
                String[] colorAndUrl = color.split(" ");
                replaced = replaced.replace("color: #" + Theme.DEFAULT.getColor(c.getId()), "color: " + colorAndUrl[0]);
                replaced = replaced.replace("background: #" + Theme.DEFAULT.getColor(c.getId()), "background: " + colorAndUrl[1]);
            }
        }
        replaced = replaced.replace("https://fonts.googleapis.com/css?family=Quicksand:300,400", Settings.THEME_FONT_STYLESHEET.toString());
        replaced = replaced.replace("''Quicksand'', sans-serif", Settings.THEME_FONT_FAMILY.toString());
        return replaced;
    }

    public String getColor(int i) {
        return colors[i];
    }

    public String replaceThemeColors(String resourceString) {
        String replaced = resourceString;
        for (Colors c : Colors.values()) {
            replaced = replaced.replace("#" + Theme.DEFAULT.getColor(c.getId()), "#" + this.getColor(c.getId()));
        }
        return replaced;
    }
}