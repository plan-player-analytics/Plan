/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.update;

import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * System for checking if new Version is available when the System initializes.
 *
 * @author Rsl1122
 */
public class VersionCheckSystem implements SubSystem {

    private final String currentVersion;
    private final Supplier<Locale> locale;
    private VersionInfo newVersionAvailable;

    public VersionCheckSystem(String currentVersion, Supplier<Locale> locale) {
        this.currentVersion = currentVersion;
        this.locale = locale;
    }

    public static VersionCheckSystem getInstance() {
        VersionCheckSystem versionCheckSystem = PlanSystem.getInstance().getVersionCheckSystem();
        Verify.nullCheck(versionCheckSystem, () -> new IllegalStateException("Version Check system has not been initialized."));
        return versionCheckSystem;
    }

    public static boolean isNewVersionAvailable() {
        return getInstance().newVersionAvailable != null;
    }

    public static String getCurrentVersion() {
        return getInstance().currentVersion;
    }

    @Override
    public void enable() {
        if (Settings.ALLOW_UPDATE.isTrue()) {
            try {
                List<VersionInfo> versions = VersionInfoLoader.load();
                if (Settings.NOTIFY_ABOUT_DEV_RELEASES.isFalse()) {
                    versions = versions.stream().filter(VersionInfo::isRelease).collect(Collectors.toList());
                }
                VersionInfo newestVersion = versions.get(0);
                if (Version.isNewVersionAvailable(new Version(currentVersion), newestVersion.getVersion())) {
                    newVersionAvailable = newestVersion;
                    String notification = locale.get().getString(
                            PluginLang.VERSION_AVAILABLE,
                            newestVersion.getVersion().toString(),
                            newestVersion.getChangeLogUrl()
                    ) + (newestVersion.isRelease() ? "" : locale.get().getString(PluginLang.VERSION_AVAILABLE_DEV));
                    Log.infoColor("§a----------------------------------------");
                    Log.infoColor("§a" + notification);
                    Log.infoColor("§a----------------------------------------");
                } else {
                    Log.info(locale.get().getString(PluginLang.VERSION_NEWEST));
                }
            } catch (IOException e) {
                Log.error(locale.get().getString(PluginLang.VERSION_FAIL_READ_VERSIONS));
            }
        } else {
            checkForNewVersion();
        }
    }

    private void checkForNewVersion() {
        String githubVersionUrl = "https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml";
        String spigotUrl = "https://www.spigotmc.org/resources/plan-player-analytics.32536/";
        try {
            boolean newVersionAvailable = Version.checkVersion(currentVersion, githubVersionUrl);
            if (!newVersionAvailable) {
                try {
                    newVersionAvailable = Version.checkVersion(currentVersion, spigotUrl);
                } catch (NoClassDefFoundError ignore) {
                    /* 1.7.4 Does not have google gson JSONParser */
                }
            }
            if (newVersionAvailable) {
                String newVersionNotification = locale.get().getString(PluginLang.VERSION_AVAILABLE_SPIGOT, spigotUrl);
                Log.infoColor("§a----------------------------------------");
                Log.infoColor("§a" + newVersionNotification);
                Log.infoColor("§a----------------------------------------");
            } else {
                Log.info(locale.get().getString(PluginLang.VERSION_NEWEST));
            }
        } catch (IOException e) {
            Log.error(locale.get().getString(PluginLang.VERSION_FAIL_READ_OLD));
        }
    }

    @Override
    public void disable() {
        /* Does not need to be closed */
    }

    public VersionInfo getNewVersionAvailable() {
        return newVersionAvailable;
    }
}
