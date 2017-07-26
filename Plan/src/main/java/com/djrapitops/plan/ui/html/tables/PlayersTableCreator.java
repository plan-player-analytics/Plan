package main.java.com.djrapitops.plan.ui.html.tables;

import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;

import java.util.List;

/**
 * @author Rsl1122
 */
public class PlayersTableCreator {

    /**
     * @param data
     * @return
     */
    public static String createSortablePlayersTable(List<UserData> data) {
        Benchmark.start("Create Players table");
        StringBuilder html = new StringBuilder();
        long now = MiscUtils.getTime();
        boolean showImages = Settings.PLAYERLIST_SHOW_IMAGES.isTrue();
        int i = 0;
//        Collections.sort(data, new UserDataLastPlayedComparator()); // Already sorted.
        for (UserData uData : data) {
            if (i >= 750) {
                break;
            }
            try {
                boolean isBanned = uData.isBanned();
                boolean isUnknown = uData.getLoginTimes() == 1;
                boolean isActive = AnalysisUtils.isActive(now, uData.getLastPlayed(), uData.getPlayTime(), uData.getLoginTimes());

                String activityString = isBanned ? Html.GRAPH_BANNED.parse()
                        : isUnknown ? Html.GRAPH_UNKNOWN.parse()
                        : isActive ? Html.GRAPH_ACTIVE.parse()
                        : Html.GRAPH_INACTIVE.parse();

                String img = showImages ? Html.MINOTAR_SMALL_IMG.parse(uData.getName()) : "";

                html.append(Html.TABLELINE_PLAYERS.parse(
                        img + Html.LINK.parse(HtmlUtils.getInspectUrl(uData.getName()), uData.getName()),
                        activityString,
                        uData.getPlayTime() + "", FormatUtils.formatTimeAmount(uData.getPlayTime()),
                        uData.getLoginTimes() + "",
                        uData.getRegistered() + "", FormatUtils.formatTimeStampYear(uData.getRegistered()),
                        uData.getLastPlayed() + "", FormatUtils.formatTimeStamp(uData.getLastPlayed()),
                        uData.getGeolocation()
                ));
            } catch (NullPointerException e) {
            }
            i++;
        }
        Benchmark.stop("Create Players table");
        return html.toString();
    }
}
