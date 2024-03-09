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
import com.djrapitops.plan.settings.config.paths.key.IntegerSetting;
import com.djrapitops.plan.settings.config.paths.key.Setting;
import com.djrapitops.plan.settings.config.paths.key.StringSetting;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link Setting} values that are in "Server" or "Plugin" section.
 *
 * @author AuroraLS3
 */
public class PluginSettings {

    public static final Setting<String> SERVER_NAME = new StringSetting("Server.ServerName", PluginSettings::validateServerName, "Plan");

    private static boolean validateServerName(String name) {
        return name != null && !name.isEmpty() && !StringUtils.containsAny(name, '/', '&', '?');
    }

    public static final Setting<String> LOCALE = new StringSetting("Plugin.Logging.Locale", "default");
    public static final Setting<Boolean> WRITE_NEW_LOCALE = new BooleanSetting("Plugin.Logging.Create_new_locale_file_on_next_enable");
    public static final Setting<Boolean> LOG_NEW_LOCALE_LINES = new BooleanSetting("Plugin.Logging.Log_untranslated_locale_keys");
    public static final Setting<Boolean> DEV_MODE = new BooleanSetting("Plugin.Logging.Dev");
    public static final Setting<Integer> KEEP_LOGS_DAYS = new IntegerSetting("Plugin.Logging.Delete_logs_after_days", Setting::timeValidator);
    public static final Setting<Boolean> CHECK_FOR_UPDATES = new BooleanSetting("Plugin.Update_notifications.Check_for_updates");
    public static final Setting<Boolean> NOTIFY_ABOUT_DEV_RELEASES = new BooleanSetting("Plugin.Update_notifications.Notify_about_DEV_releases");
    public static final Setting<Boolean> PROXY_COPY_CONFIG = new BooleanSetting("Plugin.Configuration.Allow_proxy_to_manage_settings");

    private PluginSettings() {
        /* static variable class */
    }
}