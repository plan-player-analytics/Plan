package com.djrapitops.plan.utilities.formatting.time;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plugin.api.TimeAmount;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Abstract formatter for a timestamp.
 *
 * @author Rsl1122
 */
public abstract class DateFormatter implements Formatter<Long> {

    protected final PlanConfig config;

    public DateFormatter(PlanConfig config) {
        this.config = config;
    }

    @Override
    public abstract String apply(Long value);

    protected String format(long epochMs, String format) {
        boolean useServerTime = config.isTrue(Settings.USE_SERVER_TIME);
        String locale = config.getString(Settings.LOCALE);
        Locale usedLocale = locale.equalsIgnoreCase("default") ? Locale.ENGLISH : Locale.forLanguageTag(locale);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, usedLocale);
        TimeZone timeZone = useServerTime ? TimeZone.getDefault() : TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(epochMs);
    }

    protected String replaceRecentDays(long epochMs, String format) {
        return replaceRecentDays(epochMs, format, config.getString(Settings.FORMAT_DATE_RECENT_DAYS_PATTERN));
    }

    protected String replaceRecentDays(long epochMs, String format, String pattern) {
        long now = System.currentTimeMillis();

        long fromStartOfDay = now % TimeAmount.DAY.ms();
        if (epochMs > now - fromStartOfDay) {
            format = format.replace(pattern, "'Today'");
        } else if (epochMs > now - TimeAmount.DAY.ms() - fromStartOfDay) {
            format = format.replace(pattern, "'Yesterday'");
        } else if (epochMs > now - TimeAmount.DAY.ms() * 5L) {
            format = format.replace(pattern, "EEEE");
        }
        return format;
    }

}