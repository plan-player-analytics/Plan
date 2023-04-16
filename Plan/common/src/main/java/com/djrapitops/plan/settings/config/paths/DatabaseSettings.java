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

import com.djrapitops.plan.settings.config.paths.key.IntegerSetting;
import com.djrapitops.plan.settings.config.paths.key.Setting;
import com.djrapitops.plan.settings.config.paths.key.StringSetting;
import com.djrapitops.plan.settings.config.paths.key.TimeSetting;
import com.djrapitops.plan.storage.database.DBType;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * {@link Setting} values that are in "Database" section.
 *
 * @author AuroraLS3
 */
public class DatabaseSettings {

    public static final Setting<String> TYPE = new StringSetting("Database.Type", DBType::exists);
    public static final Setting<String> MYSQL_HOST = new StringSetting("Database.MySQL.Host");
    public static final Setting<String> MYSQL_PORT = new StringSetting("Database.MySQL.Port", NumberUtils::isParsable);
    public static final Setting<String> MYSQL_USER = new StringSetting("Database.MySQL.User");
    public static final Setting<String> MYSQL_PASS = new StringSetting("Database.MySQL.Password");
    public static final Setting<String> MYSQL_DATABASE = new StringSetting("Database.MySQL.Database");
    public static final Setting<String> MYSQL_LAUNCH_OPTIONS = new StringSetting("Database.MySQL.Launch_options");
    public static final Setting<Integer> MAX_CONNECTIONS = new IntegerSetting("Database.MySQL.Max_connections", value -> value > 0);
    public static final Setting<Long> MAX_LIFETIME = new TimeSetting("Database.MySQL.Max_Lifetime");

    private DatabaseSettings() {
        /* static variable class */
    }
}