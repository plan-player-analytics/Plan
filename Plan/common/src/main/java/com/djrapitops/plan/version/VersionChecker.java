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
package com.djrapitops.plan.version;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * System for checking if new Version is available when the System initializes.
 *
 * @author AuroraLS3
 */
@Singleton
public class VersionChecker implements SubSystem {

    protected final VersionNumber currentVersion;
    protected final Locale locale;
    protected final PlanConfig config;
    protected final PluginLogger logger;
    protected final RunnableFactory runnableFactory;
    protected final ErrorLogger errorLogger;

    private static final String DOWNLOAD_ICON_HTML = "<i class=\"fa fa-fw fa-download\"></i> ";

    protected VersionInfo newVersionAvailable;

    @Inject
    public VersionChecker(
            @Named("currentVersion") String currentVersion,
            Locale locale,
            PlanConfig config,
            PluginLogger logger,
            RunnableFactory runnableFactory,
            ErrorLogger errorLogger
    ) {
        this.currentVersion = new VersionNumber(currentVersion);
        this.locale = locale;
        this.config = config;
        this.logger = logger;
        this.runnableFactory = runnableFactory;
        this.errorLogger = errorLogger;
    }

    public boolean isNewVersionAvailable() {
        return newVersionAvailable != null;
    }

    protected Optional<List<VersionInfo>> loadVersionInfo() {
        try {
            return Optional.of(VersionInfoLoader.load());
        } catch (IOException e) {
            errorLogger.warn(e, ErrorContext.builder()
                .related(locale.getString(PluginLang.VERSION_FAIL_READ_VERSIONS))
                .whatToDo("Allow Plan to check for updates from Github/versions.txt or disable update check.")
                .build());
            return Optional.empty();
        }
    }

    private void checkForUpdates() {
        loadVersionInfo().ifPresent(versions -> {
            if (config.isFalse(PluginSettings.NOTIFY_ABOUT_DEV_RELEASES)) {
                versions = Lists.filter(versions, VersionInfo::isRelease);
            }
            VersionInfo newestVersion = versions.get(0);
            if (newestVersion.getVersion().isNewerThan(currentVersion)) {
                newVersionAvailable = newestVersion;
                String notification = locale.getString(
                    PluginLang.VERSION_AVAILABLE,
                    newestVersion.getVersion().asString(),
                    newestVersion.getChangeLogUrl()
                ) + (newestVersion.isRelease() ? "" : locale.getString(PluginLang.VERSION_AVAILABLE_DEV));
                logger.info("§a----------------------------------------");
                logger.info("§a" + notification);
                logger.info("§a----------------------------------------");
            } else {
                logger.info(locale.getString(PluginLang.VERSION_NEWEST));
            }
        });
    }


    @Override
    public void enable() {
        if (config.isFalse(PluginSettings.CHECK_FOR_UPDATES)) {
            return;
        }
        runnableFactory.create(this::checkForUpdates).runTaskAsynchronously();
    }

    @Override
    public void disable() {
        /* Does not need to be closed */
    }

    public Optional<VersionInfo> getNewVersionAvailable() {
        return Optional.ofNullable(newVersionAvailable);
    }

    public Optional<String> getUpdateButton() {
        return getNewVersionAvailable().map(v -> {
            String reduceFontSize = v.getVersion().compareTo(new VersionNumber("5.2 build 999")) > 0 ?
                    "font-size: 0.95rem;" : "";
                    return "<button class=\"btn bg-white col-plan\" style=\"" + reduceFontSize +
                            "\" data-bs-target=\"#updateModal\" data-bs-toggle=\"modal\" type=\"button\">" +
                            DOWNLOAD_ICON_HTML + locale.getString(PluginLang.VERSION_UPDATE) + ": " + v.getVersion().asString() +
                            "</button>";
                }
        );
    }

    public String getCurrentVersionButton() {
        return "<button class=\"btn bg-plan\" data-bs-target=\"#updateModal\" data-bs-toggle=\"modal\" type=\"button\">" +
                getCurrentVersion() +
                "</button>";
    }

    public String getUpdateModal() {
        return getNewVersionAvailable()
                .map(v -> "<div class=\"modal-header\">" +
                        "<h5 class=\"modal-title\" id=\"updateModalLabel\">" +
                        DOWNLOAD_ICON_HTML + locale.getString(PluginLang.VERSION_UPDATE_AVAILABLE, v.getVersion().asString()) +
                        "</h5><button aria-label=\"Close\" class=\"btn-close\" data-bs-dismiss=\"modal\" type=\"button\"></button>" +
                        "</div>" + // Close modal-header
                        "<div class=\"modal-body\">" +
                        "<p>" + locale.getString(PluginLang.VERSION_CURRENT, getCurrentVersion()) + ". " + locale.getString(PluginLang.VERSION_UPDATE_INFO) +
                        (v.isRelease() ? "" : "<br>" + locale.getString(PluginLang.VERSION_UPDATE_DEV)) + "</p>" +
                        "<a class=\"btn col-plan\" href=\"" + v.getChangeLogUrl() + "\" rel=\"noopener noreferrer\" target=\"_blank\">" +
                        "<i class=\"fa fa-fw fa-list\"></i> " + locale.getString(PluginLang.VERSION_CHANGE_LOG) + "</a>" +
                        "<a class=\"btn col-plan\" href=\"" + v.getDownloadUrl() + "\" rel=\"noopener noreferrer\" target=\"_blank\">" +
                        DOWNLOAD_ICON_HTML + locale.getString(PluginLang.VERSION_DOWNLOAD, v.getVersion().asString()) + "</a>" +
                        "</div>") // Close modal-body
                .orElse("<div class=\"modal-header\">" +
                        "<h5 class=\"modal-title\" id=\"updateModalLabel\">" +
                        "<i class=\"far fa-fw fa-check-circle\"></i> " + locale.getString(PluginLang.VERSION_CURRENT, getCurrentVersion()) +
                        "</h5><button aria-label=\"Close\" class=\"btn-close\" data-bs-dismiss=\"modal\" type=\"button\"></button>" +
                        "</div>" + // Close modal-header
                        "<div class=\"modal-body\">" +
                        "<p>" + locale.getString(PluginLang.VERSION_NEWEST) + "</p>" +
                        "</div>"); // Close modal-body
    }

    public String getCurrentVersion() {
        return currentVersion.asString();
    }
}
