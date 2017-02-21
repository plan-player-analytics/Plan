package main.java.com.djrapitops.plan.ui.tables;

import java.util.Collection;
import java.util.Date;
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

    public static String createSortablePlayersTable(Collection<UserData> data) {
        String html = "";
        for (UserData uData : data) {
            html += "<tr>"
                    + "<td>" + Html.LINK.parse(HtmlUtils.getInspectUrl(uData.getName()), uData.getName()) + "</td>"
                    + "<td>" + (uData.isBanned() ? "Banned" : (AnalysisUtils.isActive(uData.getLastPlayed(), uData.getPlayTime(), uData.getLoginTimes())
                    ? "Active" : "Inactive")) + "</td>"
                    + "<td sorttable_customkey=\""+uData.getPlayTime()+"\">" + FormatUtils.formatTimeAmount(uData.getPlayTime() + "") + "</td>"
                    + "<td>" + uData.getLoginTimes() + "</td>"
                    + "<td sorttable_customkey=\""+uData.getRegistered()+"\">" + FormatUtils.formatTimeStamp(uData.getRegistered() + "") + "</td>"
                    + "<td sorttable_customkey=\""+uData.getLastPlayed()+"\">" + FormatUtils.formatTimeStamp(uData.getLastPlayed() + "") + "</td>"
                    + "<td>" + uData.getDemData().getGeoLocation() + "</td>"
                    + "</tr>";
        }
        return html;
    }
}
