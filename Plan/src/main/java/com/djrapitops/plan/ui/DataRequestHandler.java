
package com.djrapitops.plan.ui;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import com.djrapitops.plan.utilities.AnalysisUtils;
import java.util.HashMap;
import java.util.Scanner;
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
        Scanner scanner = new Scanner(plugin.getResource("player.html"));
        String html = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            html += line + "\r\n";
        }
        HashMap<String, String> replaceMap = AnalysisUtils.getInspectReplaceRules(data);
        
        for (String key : replaceMap.keySet()) {
            html = html.replaceAll(key, replaceMap.get(key));
        }
        
        return html;
    }

    public String getAnalysisHtml() {
        if (!analysisCache.isCached()) {
            return "<h1>404 Data was not found in cache</h1>";
        }
        Scanner scanner = new Scanner(plugin.getResource("analysis.html"));
        String html = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            html += line + "\r\n";
        }
        HashMap<String, String> replaceMap = AnalysisUtils.getAnalysisReplaceRules(analysisCache.getData());
        
        for (String key : replaceMap.keySet()) {
            html = html.replaceAll(key, replaceMap.get(key));
        }
        
        return html;
    }

    public boolean checkIfAnalysisIsCached() {
        return analysisCache.isCached();
    }
}
