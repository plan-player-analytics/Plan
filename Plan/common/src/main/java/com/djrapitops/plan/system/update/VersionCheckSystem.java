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
package com.djrapitops.plan.system.update;

import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.PluginSettings;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * System for checking if new Version is available when the System initializes.
 *
 * @author Rsl1122
 */
@Singleton
public class VersionCheckSystem implements SubSystem {

    private final String currentVersion;
    private final Locale locale;
    private final PlanConfig config;
    private final PluginLogger logger;

    private VersionInfo newVersionAvailable;

    @Inject
    public VersionCheckSystem(
            @Named("currentVersion") String currentVersion,
            Locale locale,
            PlanConfig config,
            PluginLogger logger
    ) {
        this.currentVersion = currentVersion;
        this.locale = locale;
        this.config = config;
        this.logger = logger;
    }

    public boolean isNewVersionAvailable() {
        return newVersionAvailable != null;
    }

    @Override
    public void enable() {
        if (config.isFalse(PluginSettings.CHECK_FOR_UPDATES)) {
            return;
        }
        try {
            List<VersionInfo> versions = VersionInfoLoader.load();
            if (config.isFalse(PluginSettings.NOTIFY_ABOUT_DEV_RELEASES)) {
                versions = versions.stream().filter(VersionInfo::isRelease).collect(Collectors.toList());
            }
            VersionInfo newestVersion = versions.get(0);
            if (Version.isNewVersionAvailable(new Version(currentVersion), newestVersion.getVersion())) {
                newVersionAvailable = newestVersion;
                String notification = locale.getString(
                        PluginLang.VERSION_AVAILABLE,
                        newestVersion.getVersion().toString(),
                        newestVersion.getChangeLogUrl()
                ) + (newestVersion.isRelease() ? "" : locale.getString(PluginLang.VERSION_AVAILABLE_DEV));
                logger.log(L.INFO_COLOR, "§a----------------------------------------");
                logger.log(L.INFO_COLOR, "§a" + notification);
                logger.log(L.INFO_COLOR, "§a----------------------------------------");
            } else {
                logger.info(locale.getString(PluginLang.VERSION_NEWEST));
            }
        } catch (IOException e) {
            logger.error(locale.getString(PluginLang.VERSION_FAIL_READ_VERSIONS));
        }
    }

    @Override
    public void disable() {
        /* Does not need to be closed */
    }

    public Optional<VersionInfo> getNewVersionAvailable() {
        return Optional.ofNullable(newVersionAvailable);
    }

    public Optional<String> getUpdateHtml() {
        return getNewVersionAvailable()
                .map(v -> v.isTrusted() ? "<a href=\"" + v.getChangeLogUrl() + "\" target=\"_blank\">" +
                        "<h4 class=\"col-green\"><i class=\"" + (v.isRelease() ? "fa fa-download" : "fab fa-dev") + "\"></i> v" + v.getVersion().getVersionString() + " available!</h4></a>" : "");
    }

    public String getCurrentVersion() {
        return currentVersion;
    }
}
