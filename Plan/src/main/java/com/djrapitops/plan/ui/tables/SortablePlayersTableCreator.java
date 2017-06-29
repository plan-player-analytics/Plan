package main.java.com.djrapitops.plan.ui.tables;

import java.util.Collections;
import java.util.List;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.comparators.UserDataLastPlayedComparator;

/**
 *
 * @author Rsl1122
 */
public class SortablePlayersTableCreator {

    /**
     *
     * @param data
     * @return
     */
    public static String createSortablePlayersTable(List<UserData> data) {
        Benchmark.start("Create Players table " + data.size());
        StringBuilder html = new StringBuilder();
        long now = MiscUtils.getTime();
        boolean showImages = Settings.PLAYERLIST_SHOW_IMAGES.isTrue() && data.size() < 3000;
        if (data.size() < 3000) {
            for (UserData uData : data) {
                try {
                    String banOunknownOactiveOinactive = uData.isBanned() ? Html.GRAPH_BANNED.parse()
                            : uData.getLoginTimes() == 1 ? Html.GRAPH_UNKNOWN.parse()
                                    : AnalysisUtils.isActive(now, uData.getLastPlayed(), uData.getPlayTime(), uData.getLoginTimes()) ? Html.GRAPH_ACTIVE.parse()
                                    : Html.GRAPH_INACTIVE.parse();
                    String img = showImages ? Html.MINOTAR_SMALL_IMG.parse(uData.getName()) : "";
                    html.append(Html.TABLELINE_PLAYERS.parse(
                            img + Html.LINK.parse(HtmlUtils.getInspectUrl(uData.getName()), uData.getName()),
                            banOunknownOactiveOinactive,
                            uData.getPlayTime() + "", FormatUtils.formatTimeAmount(uData.getPlayTime()),
                            uData.getLoginTimes() + "",
                            uData.getRegistered() + "", FormatUtils.formatTimeStampYear(uData.getRegistered()),
                            uData.getLastPlayed() + "", FormatUtils.formatTimeStamp(uData.getLastPlayed()),
                            uData.getDemData().getGeoLocation()
                    ));
                } catch (NullPointerException e) {
                }
            }
        } else {
            Collections.sort(data, new UserDataLastPlayedComparator());
            int i = 0;
            for (UserData uData : data) {
                if (i >= 3000) {
                    break;
                }
                try {
                    html.append(Html.TABLELINE_PLAYERS.parse(
                            Html.LINK.parse(HtmlUtils.getInspectUrl(uData.getName()), uData.getName()),
                            "",
                            "",
                            "",
                            uData.getRegistered() + "", FormatUtils.formatTimeStampYear(uData.getRegistered()),
                            uData.getLastPlayed() + "", FormatUtils.formatTimeStamp(uData.getLastPlayed()),
                            uData.getDemData().getGeoLocation()
                    ));
                } catch (NullPointerException e) {
                }
                i++;
            }
        }
        Benchmark.stop("Create Players table " + data.size());
        return html.toString();
    }
}
