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
package com.djrapitops.plan.system.settings.locale;

import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.paths.PluginSettings;
import com.djrapitops.plan.system.settings.locale.lang.*;
import com.djrapitops.plan.system.storage.file.PlanFiles;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * System in charge of {@link Locale}.
 *
 * @author Rsl1122
 */
@Singleton
public class LocaleSystem implements SubSystem {

    private final PlanFiles files;
    private final PlanConfig config;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    private final Locale locale;

    @Inject
    public LocaleSystem(
            PlanFiles files,
            PlanConfig config,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.files = files;
        this.config = config;
        this.logger = logger;
        this.errorHandler = errorHandler;
        this.locale = new Locale();
    }

    public static Map<String, Lang> getIdentifiers() {
        Lang[][] lang = new Lang[][]{
                CommandLang.values(),
                CmdHelpLang.values(),
                DeepHelpLang.values(),
                PluginLang.values(),
                ManageLang.values(),
                GenericLang.values(),
                CommonHtmlLang.values(),
                PlayerPageLang.values(),
                ServerPageLang.values(),
                NetworkPageLang.values(),
                ErrorPageLang.values(),
                FailReason.values()
        };

        return Arrays.stream(lang)
                .flatMap(Arrays::stream)
                .collect(Collectors.toMap(Lang::getIdentifier, Function.identity()));
    }

    @Override
    public void enable() {
        File localeFile = files.getLocaleFile();

        if (config.isTrue(PluginSettings.WRITE_NEW_LOCALE)) {
            writeNewDefaultLocale(localeFile);
        }

        Optional<Locale> loaded;
        if (localeFile.exists()) {
            loaded = loadFromFile(localeFile);
        } else {
            loaded = loadSettingLocale();
        }
        loaded.ifPresent(locale::loadFromAnotherLocale);

        LangCode langCode = locale.getLangCode();
        logger.info("Locale: '" + langCode.getName() + "' by " + langCode.getAuthors());
    }

    private void writeNewDefaultLocale(File localeFile) {
        try {
            new LocaleFileWriter(localeFile.exists() ? Locale.fromFile(localeFile) : locale).writeToFile(localeFile);
        } catch (IOException | IllegalStateException e) {
            logger.error("Failed to write new Locale file at " + localeFile.getAbsolutePath());
            errorHandler.log(L.WARN, this.getClass(), e);
        }
        resetWriteConfigSetting();
    }

    private void resetWriteConfigSetting() {
        try {
            config.set(PluginSettings.WRITE_NEW_LOCALE, false);
            config.save();
        } catch (IOException | IllegalStateException e) {
            logger.error("Failed set WriteNewLocaleFileOnEnable back to false");
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    private Optional<Locale> loadSettingLocale() {
        try {
            String setting = config.get(PluginSettings.LOCALE);
            if (!"default".equalsIgnoreCase(setting)) {
                return Optional.of(Locale.forLangCodeString(files, setting));
            }
        } catch (IOException e) {
            logger.warn("Failed to read locale from jar: " + config.get(PluginSettings.LOCALE) + ", " + e.toString());
            logger.warn("Using Default Locale as a fallback (EN)");
        }
        return Optional.empty();
    }

    private Optional<Locale> loadFromFile(File localeFile) {
        try {
            return Optional.of(Locale.fromFile(localeFile));
        } catch (IOException e) {
            logger.warn("Failed to read locale file at " + localeFile.getAbsolutePath() + ", " + e.toString());
            logger.warn("Using Default Locale as a fallback (EN)");
        }
        return Optional.empty();
    }

    @Override
    public void disable() {
        // No action necessary on disable.
    }

    public Locale getLocale() {
        return locale;
    }
}