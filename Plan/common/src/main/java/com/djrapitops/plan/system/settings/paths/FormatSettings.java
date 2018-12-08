package com.djrapitops.plan.system.settings.paths;

import com.djrapitops.plan.system.settings.paths.key.BooleanSetting;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plan.system.settings.paths.key.StringSetting;

/**
 * {@link Setting} values that are in "Formatting" section.
 *
 * @author Rsl1122
 */
public class FormatSettings {

    public static final Setting<String> DECIMALS = new StringSetting("Formatting.Decimal_points");
    public static final Setting<Boolean> DATE_RECENT_DAYS = new BooleanSetting("Formatting.Dates.Show_recent_day_names");
    public static final Setting<String> DATE_RECENT_DAYS_PATTERN = new StringSetting("Formatting.Dates.Show_recent_day_names.DatePattern");
    public static final Setting<String> DATE_FULL = new StringSetting("Formatting.Dates.Full");
    public static final Setting<String> DATE_NO_SECONDS = new StringSetting("Formatting.Dates.NoSeconds");
    public static final Setting<String> DATE_CLOCK = new StringSetting("Formatting.Dates.JustClock");
    public static final Setting<String> YEAR = new StringSetting("Formatting.Time_amount.Year");
    public static final Setting<String> YEARS = new StringSetting("Formatting.Time_amount.Years");
    public static final Setting<String> MONTH = new StringSetting("Formatting.Time_amount.Month");
    public static final Setting<String> MONTHS = new StringSetting("Formatting.Time_amount.Months");
    public static final Setting<String> DAY = new StringSetting("Formatting.Time_amount.Day");
    public static final Setting<String> DAYS = new StringSetting("Formatting.Time_amount.Days");
    public static final Setting<String> HOURS = new StringSetting("Formatting.Time_amount.Hours");
    public static final Setting<String> MINUTES = new StringSetting("Formatting.Time_amount.Minutes");
    public static final Setting<String> SECONDS = new StringSetting("Formatting.Time_amount.Seconds");
    public static final Setting<String> ZERO_SECONDS = new StringSetting("Formatting.Time_amount.Zero");

    private FormatSettings() {
        /* static variable class */
    }
}