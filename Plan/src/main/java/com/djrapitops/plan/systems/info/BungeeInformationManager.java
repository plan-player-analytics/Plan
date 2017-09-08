/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info;

import main.java.com.djrapitops.plan.bungee.PlanBungee;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.InspectPageResponse;

import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BungeeInformationManager extends InformationManager {

    public BungeeInformationManager(PlanBungee plugin) {
        usingBungeeWebServer = true;
    }

    @Override
    public void refreshAnalysis() {
        // TODO Refresh network page
    }

    public void refreshAnalysis(UUID serverUUID) {
        // TODO
    }

    @Override
    public void cachePlayer(UUID uuid) {
        PageCache.loadPage("inspectPage: " + uuid, () -> new InspectPageResponse(this, uuid));
        // TODO Player page plugin tab request
    }

    @Override
    public DataCache getDataCache() {
        return null;
    }

    @Override
    public void attemptConnection() {

    }

    @Override
    public boolean isAnalysisCached() {
        return PageCache.isCached("networkPage");
    }

    @Override
    public String getPlayerHtml(UUID uuid) {
        return null;
    }

    @Override
    public String getAnalysisHtml() {
        return null;
    }
}