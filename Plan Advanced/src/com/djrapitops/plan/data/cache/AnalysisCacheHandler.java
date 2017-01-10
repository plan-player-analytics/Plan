
package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.Plan;

/**
 *
 * @author Rsl1122
 */
public class AnalysisCacheHandler {
    private Plan plugin;
    private InspectCacheHandler inspectCache;

    public AnalysisCacheHandler(Plan plugin) {
        this.plugin = plugin;
        this.inspectCache = plugin.getInspectCache();
    }

    public boolean isCached() {
        return true;
    }
}
