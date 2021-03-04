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

/**
 * {@link Setting} values that are in "Formatting" section.
 *
 * @author AuroraLS3
 */
public class FormatSettings {

    public static final Setting<String> DECIMALS = new StringSetting("Formatting.Decimal_points");
    public static final Setting<Boolean> DATE_RECENT_DAYS = new BooleanSetting("Formatting.Dates.Show_recent_day_names");
    public static final Setting<String> DATE_RECENT_DAYS_PATTERN = new StringSetting("Formatting.Dates.Show_recent_day_names.DatePattern");
    public static final Setting<String> DATE_FULL = new StringSetting("Formatting.Dates.Full");
    public static final Setting<String> DATE_NO_SECONDS = new StringSetting("Formatting.Dates.NoSeconds");
    public static final Setting<String> DATE_CLOCK = new StringSetting("Formatting.Dates.JustClock");
    public static final Setting<String> TIMEZONE = new StringSetting("Formatting.Dates.TimeZone");
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