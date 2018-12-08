package com.djrapitops.plan.system.settings.paths;

import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plan.system.settings.paths.key.StringListSetting;
import com.djrapitops.plan.system.settings.paths.key.StringSetting;

import java.util.List;

/**
 * {@link Setting} values that are in "Plugins" section.
 *
 * @author Rsl1122
 */
public class PluginDataSettings {

    public static final Setting<String> PLUGIN_BUYCRAFT_SECRET = new StringSetting("Plugins.BuyCraft.Secret");
    public static final Setting<List<String>> HIDE_FACTIONS = new StringListSetting("Plugins.Factions.HideFactions");
    public static final Setting<List<String>> HIDE_TOWNS = new StringListSetting("Plugins.Towny.HideTowns");

    private PluginDataSettings() {
        /* static variable class */
    }
}