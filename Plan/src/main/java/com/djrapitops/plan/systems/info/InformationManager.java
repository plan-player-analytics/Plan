/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info;

import com.djrapitops.plugin.command.ISender;
import main.java.com.djrapitops.plan.api.exceptions.ParseException;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.SessionCache;
import main.java.com.djrapitops.plan.systems.webserver.pagecache.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.pagecache.PageId;

import java.io.IOException;
import java.util.*;

/**
 * Abstract layer for Bukkit and Bungee Information managers.
 * <p>
 * Manages analysis notification sending.
 *
 * @author Rsl1122
 */
public abstract class InformationManager {
    boolean usingAnotherWebServer;
    String webServerAddress;
    Map<UUID, Set<ISender>> analysisNotification;

    public InformationManager() {
        analysisNotification = new HashMap<>();
    }

    public abstract boolean attemptConnection();

    public abstract void cachePlayer(UUID uuid);

    public String getLinkTo(String target) {
        return getWebServerAddress() + target;
    }

    public abstract void refreshAnalysis(UUID serverUUID);

    public abstract DataCache getDataCache();

    public SessionCache getSessionCache() {
        return getDataCache();
    }

    public boolean isCached(UUID uuid) {
        return PageCache.isCached(PageId.PLAYER.of(uuid));
    }

    public abstract String getPlayerHtml(UUID uuid) throws ParseException;

    /**
     * Used for /server on Bukkit and /network on Bungee
     *
     * @return Is page cached.
     */
    public abstract boolean isAnalysisCached(UUID serverUUID);

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

    public abstract String[] getPluginsTabContent(UUID uuid);

    public boolean isUsingAnotherWebServer() {
        return usingAnotherWebServer;
    }

    public abstract String getWebServerAddress();

    public boolean isAuthRequired() {
        return getWebServerAddress().startsWith("https");
    }

    public abstract void analysisReady(UUID serverUUID);

    public abstract void updateNetworkPageContent();

    public abstract TreeMap<String,List<String>> getErrors() throws IOException;
}