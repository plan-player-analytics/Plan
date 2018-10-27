package com.djrapitops.plan.utilities.formatting.time;

import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.GenericLang;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.formatting.Formatter;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Abstract formatter for a timestamp.
 *
 * @author Rsl1122
 */
public abstract class DateFormatter implements Formatter<Long> {

    protected final PlanConfig config;
    protected final Locale locale;

    public DateFormatter(PlanConfig config, Locale locale) {
        this.config = config;
        this.locale = locale;
    }

    @Override
    public abstract String apply(Long value);

    protected String format(long epochMs, String format) {
        boolean useServerTime = config.isTrue(Settings.USE_SERVER_TIME);
        String locale = config.getString(Settings.LOCALE);
        java.util.Locale usedLocale = locale.equalsIgnoreCase("default")
                ? java.util.Locale.ENGLISH
                : java.util.Locale.forLanguageTag(locale);
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

        long fromStartOfDay = now % TimeUnit.DAYS.toMillis(1L);
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