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
package com.djrapitops.plan.system.settings.paths;

import com.djrapitops.plan.system.settings.paths.key.BooleanSetting;
import com.djrapitops.plan.system.settings.paths.key.IntegerSetting;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plan.system.settings.paths.key.TimeSetting;

/**
 * {@link Setting} values that are in "Time" section.
 *
 * @author Rsl1122
 */
public class TimeSettings {

    public static final Setting<Boolean> USE_SERVER_TIME = new BooleanSetting("Time.Use_server_timezone");
    public static final Setting<Long> PING_SERVER_ENABLE_DELAY = new TimeSetting("Time.Delays.Ping_server_enable_delay");
    public static final Setting<Long> PING_PLAYER_LOGIN_DELAY = new TimeSetting("Time.Delays.Ping_player_join_delay");
    public static final Setting<Long> AFK_THRESHOLD = new TimeSetting("Time.Thresholds.AFK_threshold");
    public static final Setting<Integer> ACTIVE_LOGIN_THRESHOLD = new IntegerSetting("Time.Thresholds.Activity_index.Login_threshold", Setting::timeValidator);
    public static final Setting<Long> ACTIVE_PLAY_THRESHOLD = new TimeSetting("Time.Thresholds.Activity_index.Playtime_threshold");
    public static final Setting<Long> KEEP_INACTIVE_PLAYERS = new TimeSetting("Time.Thresholds.Remove_inactive_player_data_after");
    public static final Setting<Long> ANALYSIS_REFRESH_PERIOD = new TimeSetting("Time.Periodic_tasks.Analysis_refresh_every");
    public static final Setting<Long> CLEAN_CACHE_PERIOD = new TimeSetting("Time.Periodic_tasks.Clean_caches_every");
    public static final Setting<Long> CLEAN_DATABASE_PERIOD = new TimeSetting("Time.Periodic_tasks.Clean_Database_every");
    public static final Setting<Long> CONFIG_UPDATE_INTERVAL = new TimeSetting("Time.Periodic_tasks.Check_DB_for_server_config_files_every");

    private TimeSettings() {
        /* static variable class */
    }

}