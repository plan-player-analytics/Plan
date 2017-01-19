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

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public DataRequestHandler(Plan plugin) {
        this.plugin = plugin;
        this.inspectCache = plugin.getInspectCache();
        this.analysisCache = plugin.getAnalysisCache();
    }

    /**
     * Checks if the Players data is in the inspect cache.
     *
     * @param uuid UUID of Player
     * @return true if cached.
     */
    public boolean checkIfCached(UUID uuid) {
        return inspectCache.isCached(uuid);
    }

    /**
     * Returns the player.html as string with replaced placeholders.
     *
     * @param uuid UUID of player, whose UserData is used to replace
     * placeholders with
     * @return The html
     */
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

    /**
     * Returns the analysis.html as string with replaced placeholders.
     * @return the html
     */
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

    /**
     * Checks if the AnalysisData is cached.
     * @return true if cached.
     */
    public boolean checkIfAnalysisIsCached() {
        return analysisCache.isCached();
    }
}
