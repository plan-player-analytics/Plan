package com.djrapitops.plan.system.settings.paths;

import com.djrapitops.plan.system.database.databases.DBType;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plan.system.settings.paths.key.StringSetting;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * {@link Setting} values that are in "Database" section.
 *
 * @author Rsl1122
 */
public class DatabaseSettings {

    public static final Setting<String> TYPE = new StringSetting("Database.Type", DBType::exists);
    public static final Setting<String> MYSQL_HOST = new StringSetting("Database.MySQL.Host");
    public static final Setting<String> MYSQL_PORT = new StringSetting("Database.MySQL.Port", NumberUtils::isParsable);
    public static final Setting<String> MYSQL_USER = new StringSetting("Database.MySQL.User");
    public static final Setting<String> MYSQL_PASS = new StringSetting("Database.MySQL.Password");
    public static final Setting<String> MYSQL_DATABASE = new StringSetting("Database.MySQL.Database");
    public static final Setting<String> MYSQL_LAUNCH_OPTIONS = new StringSetting("Database.MySQL.Launch_options");

    private DatabaseSettings() {
        /* static variable class */
    }
}