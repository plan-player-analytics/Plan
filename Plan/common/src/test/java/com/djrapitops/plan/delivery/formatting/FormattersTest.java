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
package com.djrapitops.plan.delivery.formatting;

import com.djrapitops.plan.delivery.formatting.time.SecondFormatter;
import com.djrapitops.plan.delivery.formatting.time.YearFormatter;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.FormatSettings;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.locale.LangCode;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.LocaleSystem;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import extension.FullSystemExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Year;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests against formatting issues.
 *
 * @author AuroraLS3
 */
@ExtendWith(FullSystemExtension.class)
class FormattersTest {

    @TestFactory
    @DisplayName("Locale for recent day names does not cause formatting issues.")
    Stream<DynamicTest> localeFormatRegressionTest(PlanConfig config, LocaleSystem localeSystem) {
        config.set(FormatSettings.DATE_RECENT_DAYS, true);
        config.set(FormatSettings.DATE_FULL, "MMM d YYYY, HH:mm:ss");
        config.set(FormatSettings.DATE_NO_SECONDS, "MMM d YYYY, HH:mm");
        config.set(FormatSettings.DATE_RECENT_DAYS_PATTERN, "MMM d YYYY");
        return Arrays.stream(LangCode.values())
                .filter(langCode -> langCode != LangCode.CUSTOM)
                .map(langCode ->
                        DynamicTest.dynamicTest("Lang " + langCode.name() + " does not cause formatting issues with plugin.generic.today or plugin.generic.yesterday lang keys.", () -> {
                            config.set(PluginSettings.LOCALE, langCode.name());
                            Locale locale = localeSystem.loadSettingLocale().orElseThrow(() -> new AssertionError("Could not load " + langCode + "locale"));
                            YearFormatter yearFormatter = new YearFormatter(config, locale);
                            SecondFormatter secondFormatter = new SecondFormatter(config, locale);
                            long today = System.currentTimeMillis();
                            assertDoesNotThrow(() -> yearFormatter.apply(today), "plugin.generic.today is causing Formatting issues for lang " + langCode);
                            assertDoesNotThrow(() -> secondFormatter.apply(today), "plugin.generic.today is causing Formatting issues for lang " + langCode);
                            long yesterday = today - TimeUnit.DAYS.toMillis(1L) - 1000L;
                            assertDoesNotThrow(() -> yearFormatter.apply(yesterday), "plugin.generic.yesterday is causing Formatting issues for lang " + langCode);
                            assertDoesNotThrow(() -> secondFormatter.apply(yesterday), "plugin.generic.yesterday is causing Formatting issues for lang " + langCode);
                        })
                );
    }

    @Test
    @DisplayName("Formatters correctly label Today, Yesterday, and Future dates.")
    void dateLabelTest(PlanConfig config) {
        config.set(PluginSettings.LOCALE, "default");
        config.set(FormatSettings.DATE_RECENT_DAYS, true);
        config.set(FormatSettings.DATE_FULL, "MMM d YYYY, HH:mm:ss");
        config.set(FormatSettings.DATE_NO_SECONDS, "MMM d YYYY, HH:mm");
        config.set(FormatSettings.DATE_RECENT_DAYS_PATTERN, "MMM d YYYY");
        config.set(FormatSettings.TIMEZONE, "UTC");

        Locale locale = new Locale();
        YearFormatter yearFormatter = new YearFormatter(config, locale);
        SecondFormatter secondFormatter = new SecondFormatter(config, locale);

        String todayLabel = locale.getString(GenericLang.TODAY).replace("'", "");
        String yesterdayLabel = locale.getString(GenericLang.YESTERDAY).replace("'", "");

        long now = System.currentTimeMillis();
        long dayMs = TimeUnit.DAYS.toMillis(1L);
        long fromStartOfDay = now % dayMs;
        long todayStart = now - fromStartOfDay;
        long yesterdayStart = todayStart - dayMs;
        long tomorrowStart = todayStart + dayMs;

        // Today
        String result = yearFormatter.apply(todayStart + 1000);
        assertTrue(result.contains(todayLabel), "Start of today should be labeled Today: " + result);
        result = yearFormatter.apply(tomorrowStart - 1000);
        assertTrue(result.contains(todayLabel), "End of today should be labeled Today: " + result);
        result = secondFormatter.apply(now);
        assertTrue(result.contains(todayLabel), "Now should be labeled Today: " + result);

        // Yesterday
        result = yearFormatter.apply(yesterdayStart + 1000);
        assertTrue(result.contains(yesterdayLabel), "Start of yesterday should be labeled Yesterday: " + result);
        result = yearFormatter.apply(todayStart - 1000);
        assertTrue(result.contains(yesterdayLabel), "End of yesterday should be labeled Yesterday: " + result);

        // Future
        String futureDate = yearFormatter.apply(tomorrowStart + 1000);
        assertFalse(futureDate.contains(todayLabel), "Future date should not be labeled Today: " + futureDate);
        assertFalse(futureDate.contains(yesterdayLabel), "Future date should not be labeled Yesterday: " + futureDate);

        // Past (Recent Days - day name)
        long fourDaysAgo = now - dayMs * 4;
        String pastDate = yearFormatter.apply(fourDaysAgo);
        assertFalse(pastDate.contains(todayLabel), "Past date should not be labeled Today: " + pastDate);
        assertFalse(pastDate.contains(yesterdayLabel), "Past date should not be labeled Yesterday: " + pastDate);
        // It should be a day name, so it shouldn't contain the year pattern if replaced by EEEE
        assertFalse(pastDate.contains(String.valueOf(Year.now().getValue())), "Past date should be replaced by day name, not contain the year: " + pastDate);
    }
}