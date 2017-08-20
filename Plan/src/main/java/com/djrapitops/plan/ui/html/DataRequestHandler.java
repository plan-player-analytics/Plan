package main.java.com.djrapitops.plan.ui.html;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.PlaceholderUtils;

import java.io.FileNotFoundException;
import java.util.UUID;

/**
 * @author Rsl1122
 */
@Deprecated //TODO Make an utility class for parsing files to give to the page cache.
public class DataRequestHandler {

    private final InspectCacheHandler inspectCache;
    private final AnalysisCacheHandler analysisCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public DataRequestHandler(Plan plugin) {
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
     *             placeholders with
     * @return The html
     */
    public String getInspectHtml(UUID uuid) {
        try {
            UserData data = inspectCache.getFromCache(uuid);
            if (data == null) {
                return "<h1>404 Data was not found in cache</h1>";
            }

            return HtmlUtils.replacePlaceholders(
                    HtmlUtils.getStringFromResource("player.html"),
                    PlaceholderUtils.getInspectReplaceRules(data)
            );
        } catch (FileNotFoundException ex) {
            return "<h1>404 player.html was not found. </h1>";
        }
    }

    /**
     * Returns the analysis.html as string with replaced placeholders.
     *
     * @return the html
     */
    @Deprecated //analysis.html has been removed //TODO server.html
    public String getAnalysisHtml() {
        try {
            if (!analysisCache.isCached()) {
                return "<h1>404 Data was not found in cache</h1>";
            }
            return HtmlUtils.replacePlaceholders(
                    HtmlUtils.getStringFromResource("analysis.html"),
                    PlaceholderUtils.getAnalysisReplaceRules(analysisCache.getData()));
        } catch (FileNotFoundException ex) {
            return "<h1>404 analysis.html was not found</h1>";
        }
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
