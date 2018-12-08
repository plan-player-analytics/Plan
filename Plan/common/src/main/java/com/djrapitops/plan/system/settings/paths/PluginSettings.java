package com.djrapitops.plan.system.settings.paths;

import com.djrapitops.plan.system.settings.paths.key.BooleanSetting;
import com.djrapitops.plan.system.settings.paths.key.IntegerSetting;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plan.system.settings.paths.key.StringSetting;

/**
 * {@link Setting} values that are in "Server" or "Plugin" section.
 *
 * @author Rsl1122
 */
public class PluginSettings {

    public static final Setting<String> SERVER_NAME = new StringSetting("Server.ServerName");
    public static final Setting<String> LOCALE = new StringSetting("Plugin.Logging.Locale");
    public static final Setting<Boolean> WRITE_NEW_LOCALE = new BooleanSetting("Plugin.Logging.Create_new_locale_file_on_next_enable");
    public static final Setting<String> DEBUG = new StringSetting("Plugin.Logging.Debug");
    public static final Setting<Boolean> DEV_MODE = new BooleanSetting("Plugin.Logging.Dev");
    public static final Setting<Integer> KEEP_LOGS_DAYS = new IntegerSetting("Plugin.Logging.Delete_logs_after_days", Setting::timeValidator);
    public static final Setting<Boolean> CHECK_FOR_UPDATES = new BooleanSetting("Plugin.Update_notifications.Check_for_updates");
    public static final Setting<Boolean> NOTIFY_ABOUT_DEV_RELEASES = new BooleanSetting("Plugin.Update_notifications.Notify_about_DEV_releases");
    public static final Setting<Boolean> BUNGEE_COPY_CONFIG = new BooleanSetting("Plugin.Configuration.Allow_bungeecord_to_manage_settings");

    private PluginSettings() {
        /* static variable class */
    }
}