/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.info;

import com.djrapitops.plugin.command.ISender;

import java.util.*;

/**
 * Abstract layer for Bukkit and Bungee Information managers.
 * <p>
 * Manages analysis notification sending.
 *
 * @author Rsl1122
 */
@Deprecated
public abstract class InformationManager {
    boolean usingAnotherWebServer;
    String webServerAddress;
    Map<UUID, Set<ISender>> analysisNotification;

    public InformationManager() {
        analysisNotification = new HashMap<>();
    }

    public abstract boolean attemptConnection();

    public String getLinkTo(String target) {
        return getWebServerAddress() + target;
    }

    public abstract void refreshAnalysis(UUID serverUUID);

    /**
     * Used for /server on Bukkit and /network on Bungee
     *
     * @return Html of a page.
     */
    public abstract String getAnalysisHtml();

    public void addAnalysisNotification(ISender sender, UUID serverUUID) {
        Set<ISender> notify = analysisNotification.getOrDefault(serverUUID, new HashSet<>());
        notify.add(sender);
        analysisNotification.put(serverUUID, notify);
    }

    public boolean isUsingAnotherWebServer() {
        return usingAnotherWebServer;
    }

    public abstract String getWebServerAddress();

    public boolean isAuthRequired() {
        return getWebServerAddress().startsWith("https");
    }

    public abstract void analysisReady(UUID serverUUID);

}