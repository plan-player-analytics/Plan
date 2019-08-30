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
package com.djrapitops.plan.settings.config.paths;

import com.djrapitops.plan.settings.config.paths.key.Setting;
import com.djrapitops.plan.settings.config.paths.key.StringListSetting;
import com.djrapitops.plan.settings.config.paths.key.StringSetting;

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