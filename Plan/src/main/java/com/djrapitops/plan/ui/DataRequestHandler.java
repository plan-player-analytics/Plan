package com.djrapitops.plan.ui;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import com.djrapitops.plan.utilities.PlaceholderUtils;
import java.util.UUID;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 *
 * @author Rsl1122
 */
public class DataRequestHandler {

    private final Plan plugin;
    private final InspectCacheHandler inspectCache;
    private final AnalysisCacheHandler analysisCache;

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
        return HtmlUtils.replacePlaceholders(
                HtmlUtils.getHtmlStringFromResource("player.html"), 
                PlaceholderUtils.getInspectReplaceRules(data)
        );
    }

    /**
     * Returns the analysis.html as string with replaced placeholders.
     *
     * @return the html
     */
    public String getAnalysisHtml() {
        if (!analysisCache.isCached()) {
            return "<h1>404 Data was not found in cache</h1>";
        }
        return HtmlUtils.replacePlaceholders(
                HtmlUtils.getHtmlStringFromResource("analysis.html"), 
                PlaceholderUtils.getAnalysisReplaceRules(analysisCache.getData())
        );
    }

    /**
     * Checks if the AnalysisData is cached.
     *
     * @return true if cached.
     */
    public boolean checkIfAnalysisIsCached() {
        return analysisCache.isCached();
    }
}
