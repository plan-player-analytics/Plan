package main.java.com.djrapitops.plan.ui.webserver.response;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.comparators.UserDataNameComparator;

import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class PlayersPageResponse extends Response {

    public PlayersPageResponse(OutputStream output, Plan plugin) {
        super(output);
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(buildContent(plugin.getInspectCache().getCachedUserData()));
    }

    public static String buildContent(List<UserData> cached) {
        StringBuilder html = new StringBuilder();
        int size = cached.size();
        html.append("<h1>Cached Players</h1><p>")
                .append(size)
                .append(" players. Use browser's Search to find players by name. (Chrome Ctrl+F)</p><table><tr>");
        cached.sort(new UserDataNameComparator());
        int i = 1;
        for (UserData userData : cached) {
            String name = userData.getName();
            String link = Html.LINK.parse(HtmlUtils.getRelativeInspectUrl(name), name);
            html.append("<td>").append(link).append("</td>");
            if (i < size) {
                if (i % 8 == 0) {
                    html.append("</tr><tr>");
                }
            }
            i++;
        }
        html.append("</tr></table>");
        return html.toString();
    }
}
