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
import extension.FullSystemExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
}