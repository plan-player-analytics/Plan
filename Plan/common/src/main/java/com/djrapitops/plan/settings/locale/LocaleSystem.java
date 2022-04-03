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
package com.djrapitops.plan.settings.locale;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.delivery.web.WebAssetVersions;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.locale.lang.*;
import com.djrapitops.plan.storage.file.FileResource;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

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
 * @author AuroraLS3
 */
@Singleton
public class LocaleSystem implements SubSystem {

    private final PlanFiles files;
    private final PlanConfig config;
    private final WebAssetVersions webAssetVersions;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    private final Locale locale;

    @Inject
    public LocaleSystem(
            PlanFiles files,
            PlanConfig config,
            WebAssetVersions webAssetVersions,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.files = files;
        this.config = config;
        this.webAssetVersions = webAssetVersions;
        this.logger = logger;
        this.errorLogger = errorLogger;
        this.locale = new Locale();
    }

    public static Map<String, Lang> getIdentifiers() {
        return Arrays.stream(getValuesArray())
                .flatMap(Arrays::stream)
                .collect(Collectors.toMap(Lang::getIdentifier, Function.identity()));
    }

    public static Map<String, Lang> getKeys() {
        return Arrays.stream(getValuesArray())
                .flatMap(Arrays::stream)
                .collect(Collectors.toMap(Lang::getKey, Function.identity()));
    }

    private static Lang[][] getValuesArray() {
        return new Lang[][]{
                CommandLang.values(),
                DeepHelpLang.values(),
                ErrorPageLang.values(),
                FailReason.values(),
                FilterLang.values(),
                GenericLang.values(),
                HelpLang.values(),
                HtmlLang.values(),
                JSLang.values(),
                PluginLang.values(),
        };
    }

    @Override
    public void enable() {
        convertFromLegacyFormat();

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
            Locale writing = loadSettingLocale().orElse(locale);
            if (localeFile.exists()) {
                writing.putAll(Locale.fromFile(localeFile));
            }
            new LocaleFileWriter(writing).writeToFile(localeFile);
        } catch (IOException | IllegalStateException e) {
            errorLogger.error(e, ErrorContext.builder().whatToDo("Fix write permissions to " + localeFile.getAbsolutePath()).build());
        }
        resetWriteConfigSetting();
    }

    private void resetWriteConfigSetting() {
        try {
            config.set(PluginSettings.WRITE_NEW_LOCALE, false);
            config.save();
        } catch (IOException | IllegalStateException e) {
            errorLogger.error(e, ErrorContext.builder().whatToDo("Fix write permissions to " + config.getConfigFilePath()).build());
        }
    }

    private void convertFromLegacyFormat() {
        File oldCustomFile = files.getFileFromPluginFolder("locale.txt");
        if (!files.getLocaleFile().exists() && oldCustomFile.exists()) {
            try {
                logger.info("Converting locale.txt to yml...");
                Locale loaded = new LocaleFileReader(new FileResource("locale.txt", oldCustomFile)).loadLegacy(LangCode.CUSTOM);
                new LocaleFileWriter(loaded).writeToFile(files.getLocaleFile());
            } catch (IOException e) {
                errorLogger.error(e, ErrorContext.builder().whatToDo("Fix write permissions to " + files.getLocaleFile().toString()).build());
            }
        }

        for (LangCode code : LangCode.values()) {
            if (code == LangCode.CUSTOM) continue;
            File oldFile = files.getFileFromPluginFolder("locale_" + code + ".txt");
            if (!files.getFileFromPluginFolder(code.getFileName()).exists() && oldFile.exists()) {
                try {
                    logger.info("Converting " + oldFile.getName() + " to yml...");
                    Locale loaded = new LocaleFileReader(new FileResource(oldFile.getName(), oldFile)).loadLegacy(LangCode.CUSTOM);
                    new LocaleFileWriter(loaded).writeToFile(files.getFileFromPluginFolder(code.getFileName()));
                } catch (IOException e) {
                    errorLogger.error(e, ErrorContext.builder().whatToDo("Fix write permissions to " + files.getFileFromPluginFolder(code.getFileName()).toString()).build());
                }
            }
        }
    }

    private Optional<Locale> loadSettingLocale() {
        try {
            String setting = config.get(PluginSettings.LOCALE);
            if ("write-all".equalsIgnoreCase(setting)) {
                for (LangCode code : LangCode.values()) {
                    if (code == LangCode.CUSTOM) continue;
                    Locale writing = Locale.forLangCode(code, files);
                    new LocaleFileWriter(writing).writeToFile(
                            files.getDataDirectory().resolve("locale_" + code.name() + ".yml").toFile()
                    );
                }

                return Optional.empty();
            }
            if (!"default".equalsIgnoreCase(setting)) {
                return Optional.of(Locale.forLangCodeString(files, setting));
            }
        } catch (IOException e) {
            logger.warn("Failed to read locale from jar: " + config.get(PluginSettings.LOCALE) + ", " + e);
            logger.warn("Using Default Locale as a fallback (EN)");
        }
        return Optional.empty();
    }

    private Optional<Locale> loadFromFile(File localeFile) {
        try {
            return Optional.of(Locale.fromFile(localeFile));
        } catch (IOException e) {
            logger.warn("Failed to read locale file at " + localeFile.getAbsolutePath() + ", " + e);
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

    public long getLocaleVersion() {
        return webAssetVersions.getLatestWebAssetVersion().orElse(0L);
    }

    public Optional<Long> getCustomLocaleVersion() {
        File localeFile = files.getLocaleFile();
        if (!localeFile.exists()) {
            return Optional.empty();
        }
        return Optional.of(localeFile.lastModified());
    }
}