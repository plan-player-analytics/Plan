/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.SessionCache;
import main.java.com.djrapitops.plan.systems.info.parsing.UrlParser;

import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class InformationManager {
    // TODO Class that manages ALL information for API, WebAPI requests, Command Caching etc.
    private final Plan plugin;
    private final Database db;

    private final DataCache dataCache;
    private final SessionCache sessionCache;

    private boolean usingBungeeWebServer;
    private String webServerAddress;

    public InformationManager(Plan plugin) {
        this.plugin = plugin;
        db = plugin.getDB();

        plugin.getServerInfoManager().getBungeeConnectionAddress()
                .ifPresent(address -> webServerAddress = address);

        dataCache = new DataCache(plugin);
        sessionCache = new SessionCache(plugin);

        if (webServerAddress != null) {
            attemptBungeeConnection();
        }
    }

    public void attemptBungeeConnection() {
        // TODO WebAPI bungee connection check
    }

    public void cachePlayer(UUID uuid) {
        plugin.addToProcessQueue(); // TODO Player page information parser
        // TODO Player page plugin tab request
    }

    public UrlParser getLinkTo(String target) {
        if (webServerAddress != null) {
            return new UrlParser(webServerAddress).target(target);
        } else {
            return new UrlParser("");
        }
    }

    public void refreshAnalysis() {
        plugin.addToProcessQueue(); // TODO Analysis, PluginData
    }

    public DataCache getDataCache() {
        return dataCache;
    }

    public SessionCache getSessionCache() {
        return sessionCache;
    }
}