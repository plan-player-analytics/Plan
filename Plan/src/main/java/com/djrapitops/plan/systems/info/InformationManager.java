/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info;

import com.djrapitops.plugin.command.ISender;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.SessionCache;
import main.java.com.djrapitops.plan.systems.info.parsing.UrlParser;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public abstract class InformationManager {
    boolean usingBungeeWebServer;
    String webServerAddress;
    Set<ISender> analysisNotification;

    public InformationManager() {
        analysisNotification = new HashSet<>();
    }

    public abstract boolean attemptConnection();

    public abstract void cachePlayer(UUID uuid);

    public UrlParser getLinkTo(String target) {
        if (webServerAddress != null) {
            return new UrlParser(webServerAddress).target(target);
        } else {
            return new UrlParser("");
        }
    }

    public abstract void refreshAnalysis();

    public abstract DataCache getDataCache();

    public SessionCache getSessionCache() {
        return getDataCache();
    }

    public boolean isCached(UUID uuid) {
        return PageCache.isCached("inspectPage: " + uuid);
    }

    public abstract String getPlayerHtml(UUID uuid);

    /**
     * Used for /server on Bukkit and /network on Bungee
     *
     * @return Is page cached.
     */
    public abstract boolean isAnalysisCached();

    /**
     * Used for /server on Bukkit and /network on Bungee
     *
     * @return Html of a page.
     */
    public abstract String getAnalysisHtml();

    public void addAnalysisNotification(ISender sender) {
        analysisNotification.add(sender);
    }

    public abstract String getPluginsTabContent(UUID uuid);

    public boolean isUsingBungeeWebServer() {
        return usingBungeeWebServer;
    }
}