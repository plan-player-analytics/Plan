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
package com.djrapitops.plan.settings.config;

import com.djrapitops.plan.delivery.domain.datatransfer.preferences.GraphThresholds;
import com.djrapitops.plan.delivery.domain.datatransfer.preferences.Preferences;
import com.djrapitops.plan.delivery.domain.datatransfer.preferences.TimeFormat;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.settings.config.paths.FormatSettings;
import com.djrapitops.plan.settings.config.paths.key.Setting;
import com.djrapitops.plan.storage.file.PlanFiles;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Plan configuration file.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlanConfig extends Config {

    private final PlanFiles files;
    private final ExtensionSettings extensionSettings;
    private final ResourceSettings resourceSettings;
    private final WorldAliasSettings worldAliasSettings;
    private final PluginLogger logger;

    @Inject
    public PlanConfig(
            PlanFiles files,
            WorldAliasSettings worldAliasSettings,
            PluginLogger logger
    ) {
        this(files.getConfigFile(), files, worldAliasSettings, logger);
    }

    // For testing
    public PlanConfig(
            File configFile,
            PlanFiles files,
            WorldAliasSettings worldAliasSettings,
            PluginLogger logger
    ) {
        super(configFile);

        this.files = files;
        this.extensionSettings = new ExtensionSettings(this);
        this.resourceSettings = new ResourceSettings(files, this);
        this.worldAliasSettings = worldAliasSettings;
        this.logger = logger;
    }

    public <T> T get(Setting<T> setting) {
        T value = setting.getValueFrom(this);
        if (setting.isInvalid(value)) {
            T defaultValue = setting.getDefaultValue();
            if (defaultValue == null) {
                throw new IllegalStateException(
                        "Config value for " + setting.getPath() + " has a bad value: '" + value + "'"
                );
            } else {
                logger.warn("Config value for " + setting.getPath() + " has a bad value: '" + value + "', using '" + defaultValue + "'");
                return defaultValue;
            }
        }
        return value;
    }

    public <T> T getOrDefault(Setting<T> setting, T defaultValue) {
        try {
            return get(setting);
        } catch (IllegalStateException e) {
            logger.warn(e.getMessage() + ", using '" + defaultValue + "'");
            return defaultValue;
        }
    }

    public boolean isTrue(Setting<Boolean> setting) {
        return Boolean.TRUE.equals(get(setting));
    }

    public boolean isFalse(Setting<Boolean> setting) {
        return !isTrue(setting);
    }

    public <T> void set(Setting<T> setting, T value) {
        set(setting.getPath(), value);
    }

    public TimeZone getTimeZone() {
        String timeZone = get(FormatSettings.TIMEZONE);
        Optional<TimeZone> foundTZ = TimeZoneUtility.parseTimeZone(timeZone);
        return foundTZ.orElse(TimeZone.getTimeZone(ZoneId.of("UTC")));
    }

    public double getTimeZoneOffsetHours() {
        int offsetMs = getTimeZone().getOffset(System.currentTimeMillis());
        int hourMs = (int) TimeUnit.HOURS.toMillis(1L);
        return -offsetMs * 1.0 / hourMs;
    }

    public Path getPageExportPath() {
        Path exportDirectory = Paths.get(get(ExportSettings.HTML_EXPORT_PATH));
        Path customizationDirectory = resourceSettings.getCustomizationDirectory();

        if (exportDirectory.toAbsolutePath().equals(customizationDirectory.toAbsolutePath())) {
            logger.warn("'" + ExportSettings.HTML_EXPORT_PATH.getPath() + "' can not be '/Plan/web/' directory, using '/Plan/Analysis Results' as fallback.");
            exportDirectory = files.getDataDirectory().resolve("Analysis Results");
        }

        return exportDirectory.isAbsolute()
                ? exportDirectory
                : files.getDataDirectory().resolve(exportDirectory);
    }

    public Path getJSONExportPath() {
        Path exportDirectory = Paths.get(get(ExportSettings.JSON_EXPORT_PATH));
        return exportDirectory.isAbsolute()
                ? exportDirectory
                : files.getDataDirectory().resolve(exportDirectory);
    }

    public ExtensionSettings getExtensionSettings() {
        return extensionSettings;
    }

    public ResourceSettings getResourceSettings() {
        return resourceSettings;
    }

    public WorldAliasSettings getWorldAliasSettings() {
        return worldAliasSettings;
    }

    public Preferences getDefaultPreferences() {
        return Preferences.builder()
                .withDateFormatFull(get(FormatSettings.DATE_FULL))
                .withDateFormatNoSeconds(get(FormatSettings.DATE_NO_SECONDS))
                .withDateFormatClock(get(FormatSettings.DATE_CLOCK))
                .withRecentDaysInDateFormat(isTrue(FormatSettings.DATE_RECENT_DAYS))
                .withDecimalFormat(get(FormatSettings.DECIMALS))
                .withFirstDay(1) // 1 is Monday
                .withTimeFormat(TimeFormat.builder()
                        .withYear(get(FormatSettings.YEAR))
                        .withYears(get(FormatSettings.YEARS))
                        .withMonth(get(FormatSettings.MONTH))
                        .withMonths(get(FormatSettings.MONTHS))
                        .withDay(get(FormatSettings.DAY))
                        .withDays(get(FormatSettings.DAYS))
                        .withHours(get(FormatSettings.HOURS))
                        .withMinutes(get(FormatSettings.MINUTES))
                        .withSeconds(get(FormatSettings.SECONDS))
                        .withZero(get(FormatSettings.ZERO_SECONDS))
                        .build())
                .withPlayerHeadImageUrl(get(DisplaySettings.PLAYER_HEAD_IMG_URL))
                .withTpsThresholds(new GraphThresholds(
                        get(DisplaySettings.GRAPH_TPS_THRESHOLD_HIGH),
                        get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED)))
                .withDiskThresholds(new GraphThresholds(
                        get(DisplaySettings.GRAPH_DISK_THRESHOLD_HIGH),
                        get(DisplaySettings.GRAPH_DISK_THRESHOLD_MED)))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}