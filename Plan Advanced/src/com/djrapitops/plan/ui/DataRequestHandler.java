
package com.djrapitops.plan.ui;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import java.util.UUID;

/**
 *
 * @author Rsl1122
 */
public class DataRequestHandler {
    private Plan plugin;
    private InspectCacheHandler inspectCache;
    private AnalysisCacheHandler analysisCache;

    public DataRequestHandler(Plan plugin) {
        this.plugin = plugin;
        this.inspectCache = plugin.getInspectCache();
        this.analysisCache = plugin.getAnalysisCache();
    }

    public boolean checkIfCached(UUID uuid) {
        return inspectCache.getCache().containsKey(uuid);
    }

    public String getDataHtml(UUID uuid) {
        UserData data = inspectCache.getFromCache(uuid);
        if (data == null) {
            return "<h1>404 Data was not found in cache</h1>";
        }
        return "Test Successful";
    }

    public String getAnalysisHtml() {
        return "Test Successful";
    }

    public boolean checkIfAnalysisIsCached() {
        return analysisCache.isCached();
    }
}
