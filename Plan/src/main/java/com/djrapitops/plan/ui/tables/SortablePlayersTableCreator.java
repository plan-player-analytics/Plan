package main.java.com.djrapitops.plan.ui.tables;

import java.util.Collection;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

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
    public static String createSortablePlayersTable(Collection<UserData> data) {
        Benchmark.start("Create Players table "+data.size());
        String html = "";
        long now = MiscUtils.getTime();
        for (UserData uData : data) {
            try {
                String banOunknownOactiveOinactive = uData.isBanned() ? Html.GRAPH_BANNED.parse()
                        : uData.getLoginTimes() == 1 ? Html.GRAPH_UNKNOWN.parse()
                                : AnalysisUtils.isActive(now, uData.getLastPlayed(), uData.getPlayTime(), uData.getLoginTimes()) ? Html.GRAPH_ACTIVE.parse()
                                : Html.GRAPH_INACTIVE.parse();

                html += Html.TABLELINE_PLAYERS.parse(
                        Html.MINOTAR_SMALL_IMG.parse(uData.getName()) + Html.LINK.parse(HtmlUtils.getInspectUrl(uData.getName()), uData.getName()),
                        banOunknownOactiveOinactive,
                        uData.getPlayTime() + "", FormatUtils.formatTimeAmount(uData.getPlayTime()),
                        uData.getLoginTimes() + "",
                        uData.getRegistered() + "", FormatUtils.formatTimeStamp(uData.getRegistered()),
                        uData.getLastPlayed() + "", FormatUtils.formatTimeStamp(uData.getLastPlayed()),
                        uData.getDemData().getGeoLocation()
                );
            } catch (NullPointerException e) {
            }
        }
        Benchmark.stop("Create Players table "+data.size());
        return html;
    }
}
