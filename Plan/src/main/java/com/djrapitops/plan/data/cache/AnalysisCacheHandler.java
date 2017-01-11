
package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.utilities.Analysis;

/**
 *
 * @author Rsl1122
 */
public class AnalysisCacheHandler {
    private Plan plugin;
    private AnalysisData cache;
    private Analysis analysis;

    public AnalysisCacheHandler(Plan plugin) {
        this.plugin = plugin;
        analysis = new Analysis(plugin);
    }
    
    public void updateCache() {
        cache = null;
        analysis.analyze(this);
    }
    
    public void cache(AnalysisData data) {
        cache = data;
    }
    
    public AnalysisData getData() {
        return cache;
    }

    public boolean isCached() {
        return (cache != null);
    }
}
