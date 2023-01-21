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
package com.djrapitops.plan.delivery.formatting.time;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.FormatSettings;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.GenericLang;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Abstract formatter for a timestamp.
 *
 * @author AuroraLS3
 */
public abstract class DateFormatter implements Formatter<Long> {

    protected final PlanConfig config;
    protected final Locale locale;

    protected DateFormatter(PlanConfig config, Locale locale) {
        this.config = config;
        this.locale = locale;
    }

    protected String format(long epochMs, String format) {
        String localeSetting = config.get(PluginSettings.LOCALE);
        java.util.Locale usedLocale = "default".equalsIgnoreCase(localeSetting)
                ? java.util.Locale.ENGLISH
                : java.util.Locale.forLanguageTag(localeSetting);
        return format(epochMs, format, usedLocale);
    }

    protected String format(long epochMs, String format, java.util.Locale usedLocale) {
        TimeZone timeZone = config.getTimeZone();
        return format(epochMs, format, usedLocale, timeZone);
    }

    protected String format(long epochMs, String format, java.util.Locale usedLocale, TimeZone timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, usedLocale);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(epochMs);
    }

    protected String replaceRecentDays(long epochMs, String format) {
        return replaceRecentDays(epochMs, format, config.get(FormatSettings.DATE_RECENT_DAYS_PATTERN));
    }

    protected String replaceRecentDays(long epochMs, String format, String pattern) {
        long now = System.currentTimeMillis();

        TimeZone timeZone = config.getTimeZone();
        int offset = timeZone.getOffset(epochMs);

        // Time since Start of day: UTC + Timezone % 24 hours
        long fromStartOfDay = (now + offset) % TimeUnit.DAYS.toMillis(1L);
        if (epochMs > now - fromStartOfDay) {
            format = format.replace(pattern, locale.getString(GenericLang.TODAY));
        } else if (epochMs > now - TimeUnit.DAYS.toMillis(1L) - fromStartOfDay) {
            format = format.replace(pattern, locale.getString(GenericLang.YESTERDAY));
        } else if (epochMs > now - TimeUnit.DAYS.toMillis(5L)) {
            format = format.replace(pattern, "EEEE");
        }
        return format;
    }

}