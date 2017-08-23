package main.java.com.djrapitops.plan.ui.html;

import main.java.com.djrapitops.plan.Plan;

import java.util.UUID;

/**
 * @author Rsl1122
 */
@Deprecated //TODO Make an utility class for parsing files to give to the page cache.
public class DataRequestHandler {

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    @Deprecated
    public DataRequestHandler(Plan plugin) {
    }

    /**
     * Checks if the Players data is in the inspect cache.
     *
     * @param uuid UUID of Player
     * @return true if cached.
     */
    @Deprecated
    public boolean checkIfCached(UUID uuid) {
        // TODO Check from PageCache
        return false;
    }

    /**
     * Returns the player.html as string with replaced placeholders.
     *
     * @param uuid UUID of player, whose UserInfo is used to replace
     *             placeholders with
     * @return The html
     */
    @Deprecated
    public String getInspectHtml(UUID uuid) {
        // TODO Get from PageCache
        return "";
    }

    /**
     * Returns the server.html as string with replaced placeholders.
     *
     * @return the html
     */
    @Deprecated
    public String getServerHtml() {
        // TODO Get from PageCache
        return "";
    }

    /**
     * Checks if the AnalysisData is cached.
     *
     * @return true if cached.
     */
    @Deprecated
    public boolean checkIfAnalysisIsCached() {
        // TODO Check from PageCache
        return false;
    }
}
