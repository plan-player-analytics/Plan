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

    private TimeSettings() {
        /* static variable class */
    }

}