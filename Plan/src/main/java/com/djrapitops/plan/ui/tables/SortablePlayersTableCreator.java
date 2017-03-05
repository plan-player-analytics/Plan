package main.java.com.djrapitops.plan.ui.tables;

import java.util.Collection;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

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
        String html = "";
        for (UserData uData : data) {
            try {
                String bORaORi = uData.isBanned() ? Html.GRAPH_BANNED.parse()
                        : (AnalysisUtils.isActive(uData.getLastPlayed(), uData.getPlayTime(), uData.getLoginTimes())
                        ? Html.GRAPH_ACTIVE.parse() : Html.GRAPH_INACTIVE.parse());

                html += Html.TABLELINE_PLAYERS.parse("<img style=\"float: left; padding: 2px 2px 0px 2px\" alt=\""+uData.getName()+"\" src=\"https://minotar.net/avatar/"+uData.getName()+"/19\"> "+Html.LINK.parse(HtmlUtils.getInspectUrl(uData.getName()), uData.getName()), bORaORi,
                        uData.getPlayTime() + "", FormatUtils.formatTimeAmount(uData.getPlayTime() + ""),
                        uData.getLoginTimes() + "",
                        uData.getRegistered() + "", FormatUtils.formatTimeStamp(uData.getRegistered() + ""),
                        uData.getLastPlayed() + "", FormatUtils.formatTimeStamp(uData.getLastPlayed() + ""),
                        uData.getDemData().getGeoLocation()
                );
            } catch (NullPointerException e) {                
            }
        }
        return html;
    }
}
