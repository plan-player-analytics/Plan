/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.update;

import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
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
        if (config.isFalse(Settings.CHECK_FOR_UPDATES)) {
            return;
        }
        try {
            List<VersionInfo> versions = VersionInfoLoader.load();
            if (config.isFalse(Settings.NOTIFY_ABOUT_DEV_RELEASES)) {
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
                        "<h4 class=\"col-green\"><i class=\"fa fa-" + (v.isRelease() ? "download" : "dev") + "\"></i> Update available!</h4></a>" : "");
    }

    public String getCurrentVersion() {
        return currentVersion;
    }
}
