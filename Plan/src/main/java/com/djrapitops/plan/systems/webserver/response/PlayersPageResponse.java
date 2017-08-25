package main.java.com.djrapitops.plan.systems.webserver.response;

import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.utilities.comparators.UserDataNameComparator;
import main.java.com.djrapitops.plan.utilities.html.Html;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;

import java.util.List;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PlayersPageResponse extends Response {

    public PlayersPageResponse() {
        super.setHeader("HTTP/1.1 200 OK");
//        super.setContent(buildContent(plugin.getInspectCache().getCachedUserData()));
    }

    public static String buildContent(List<UserInfo> cached) {
        StringBuilder html = new StringBuilder("<!DOCTYPE html><html><body><h1>Cached Players</h1><p>");
        int size = cached.size();

        html.append(size)
                .append(" players. Use browser's Search to find players by name. (Chrome Ctrl+F)</p><table><tr>");

        cached.sort(new UserDataNameComparator());

        int i = 1;
        for (UserInfo userInfo : cached) {
            String name = userInfo.getName();
            String link = Html.LINK.parse(HtmlUtils.getRelativeInspectUrl(name), name);

            html.append("<td>").append(link).append("</td>");

            if (i < size && i % 8 == 0) {
                html.append("</tr><tr>");
            }
            i++;
        }

        html.append("</tr></table></body></html>");
        return html.toString();
    }
}
