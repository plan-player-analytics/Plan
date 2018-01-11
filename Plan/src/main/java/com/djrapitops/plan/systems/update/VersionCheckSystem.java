/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.update;

import com.djrapitops.plan.systems.SubSystem;
import com.djrapitops.plan.systems.Systems;
import com.djrapitops.plugin.api.Priority;
import com.djrapitops.plugin.api.systems.NotificationCenter;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.IOException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class VersionCheckSystem implements SubSystem {

    private final String currentVersion;
    private boolean newVersionAvailable = false;

    public VersionCheckSystem(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public static VersionCheckSystem getInstance() {
        return Systems.getInstance().getVersionCheckSystem();
    }

    @Override
    public void init() {
        checkForNewVersion();
    }

    private void checkForNewVersion() {
        String githubVersionUrl = "https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml";
        String spigotUrl = "https://www.spigotmc.org/resources/plan-player-analytics.32536/";
        try {
            newVersionAvailable = Version.checkVersion(currentVersion, githubVersionUrl)
                    || Version.checkVersion(currentVersion, spigotUrl);
            if (newVersionAvailable) {
                String newVersionNotification = "New Version is available at " + spigotUrl;
                Log.infoColor("§a----------------------------------------");
                Log.infoColor("§a"+newVersionNotification);
                Log.infoColor("§a----------------------------------------");
                NotificationCenter.addNotification(Priority.HIGH, newVersionNotification);
            } else {
                Log.info("You're using the latest version.");
            }
        } catch (IOException e) {
            Log.error("Failed to check newest version number");
        }
    }

    @Override
    public void close() {
        /* Does not need to be closed */
    }

    public static boolean isNewVersionAvailable() {
        return getInstance().newVersionAvailable;
    }
}