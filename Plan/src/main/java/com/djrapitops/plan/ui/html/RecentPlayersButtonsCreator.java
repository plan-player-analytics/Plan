package main.java.com.djrapitops.plan.ui.html;

import main.java.com.djrapitops.plan.utilities.HtmlUtils;

import java.util.List;

/**
 *
 * @author Rsl1122
 */
public class RecentPlayersButtonsCreator {

    /**
     * Creates recent players buttons inside a p-tag.
     *
     * @param names The name of players sorted by last playtime.
     * @param limit How many players will be shown
     * @return html p-tag list of recent logins.
     */
    public static String createRecentLoginsButtons(List<String> names, int limit) {
        StringBuilder html = new StringBuilder();
        html.append("<p>");
        for (int i = 0; i < names.size(); i++) {
            if (i < limit) {
                String name = names.get(i);
                html.append(Html.BUTTON.parse(HtmlUtils.getInspectUrl(name), name));
                html.append(" ");
            }
        }
        html.append("</p>");
        return html.toString();
    }
}
