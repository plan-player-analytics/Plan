package com.djrapitops.plan.system.locale;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.locale.lang.*;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.auth.FailReason;
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

    private final PlanPlugin plugin;
    private final PlanFiles files;
    private final PlanConfig config;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    private final Locale locale;

    @Inject
    public LocaleSystem(
            PlanPlugin plugin,
            PlanFiles files,
            PlanConfig config,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.plugin = plugin;
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
                FailReason.values(),
                HealthInfoLang.values()
        };

        return Arrays.stream(lang)
                .flatMap(Arrays::stream)
                .collect(Collectors.toMap(Lang::getIdentifier, Function.identity()));
    }

    @Override
    public void enable() {
        File localeFile = files.getLocaleFile();

        if (config.isTrue(Settings.WRITE_NEW_LOCALE)) {
            writeNewDefaultLocale(localeFile);
        }

        Optional<Locale> loaded;
        if (localeFile.exists()) {
            loaded = loadFromFile(localeFile);
        } else {
            loaded = loadSettingLocale();
        }
        loaded.ifPresent(locale::loadFromAnotherLocale);
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
            config.set(Settings.WRITE_NEW_LOCALE, false);
            config.save();
        } catch (IOException | IllegalStateException e) {
            logger.error("Failed set WriteNewLocaleFileOnEnable back to false");
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    private Optional<Locale> loadSettingLocale() {
        try {
            String setting = config.getString(Settings.LOCALE);
            if (!setting.equalsIgnoreCase("default")) {
                return Optional.of(Locale.forLangCodeString(plugin, setting));
            }
        } catch (IOException e) {
            logger.warn("Failed to read locale from jar: " + config.getString(Settings.LOCALE) + ", " + e.toString());
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