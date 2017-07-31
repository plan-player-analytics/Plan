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
     * Constructor used to hide the public constructor
     */
    private PlayersTableCreator() {
        throw new IllegalStateException("Utility class");
    }

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
                        String.valueOf(uData.getPlayTime()), FormatUtils.formatTimeAmount(uData.getPlayTime()),
                        String.valueOf(uData.getLoginTimes()),
                        String.valueOf(uData.getRegistered()), FormatUtils.formatTimeStampYear(uData.getRegistered()),
                        String.valueOf(uData.getLastPlayed()), FormatUtils.formatTimeStamp(uData.getLastPlayed()),
                        String.valueOf(uData.getGeolocation())
                ));
            } catch (NullPointerException ignored) {
            }

            i++;
        }

        Benchmark.stop("Create Players table");
        return html.toString();
    }
}
