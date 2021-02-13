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

import com.djrapitops.plan.settings.config.paths.key.BooleanSetting;
import com.djrapitops.plan.settings.config.paths.key.Setting;
import com.djrapitops.plan.settings.config.paths.key.StringSetting;
import com.djrapitops.plan.settings.config.paths.key.TimeSetting;

/**
 * {@link Setting} values that are in "Export" section.
 *
 * @author AuroraLS3
 */
public class ExportSettings {

    public static final Setting<String> HTML_EXPORT_PATH = new StringSetting("Export.HTML_Export_path");
    public static final Setting<String> JSON_EXPORT_PATH = new StringSetting("Export.JSON_Export_path");
    public static final Setting<Boolean> PLAYER_PAGES = new BooleanSetting("Export.Parts.Player_pages");
    public static final Setting<Boolean> PLAYER_JSON = new BooleanSetting("Export.Parts.Player_JSON");
    public static final Setting<Boolean> PLAYERS_PAGE = new BooleanSetting("Export.Parts.Players_page");
    public static final Setting<Boolean> SERVER_PAGE = new BooleanSetting("Export.Parts.Server_page");
    public static final Setting<Boolean> SERVER_JSON = new BooleanSetting("Export.Parts.Server_JSON");
    public static final Setting<Boolean> EXPORT_ON_ONLINE_STATUS_CHANGE = new BooleanSetting("Export.Export_player_on_login_and_logout");
    public static final Setting<Long> EXPORT_PERIOD = new TimeSetting("Export.Server_refresh_period");

    private ExportSettings() {
        /* static variable class */
    }
}