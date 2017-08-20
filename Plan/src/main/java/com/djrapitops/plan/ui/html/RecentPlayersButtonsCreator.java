package main.java.com.djrapitops.plan.ui.html;

import main.java.com.djrapitops.plan.utilities.HtmlUtils;

import java.util.List;

/**
 * @author Rsl1122
 */
@Deprecated
public class RecentPlayersButtonsCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private RecentPlayersButtonsCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates recent players buttons inside a p-tag.
     *
     * @param names The name of players sorted by last playtime.
     * @param limit How many players will be shown
     * @return html p-tag list of recent log-ins.
     */
    public static String createRecentLoginsButtons(List<String> names, int limit) {
        StringBuilder html = new StringBuilder("<p>");

        int i = 0;
        for (String name : names) {
            if (i >= limit) {
                break;
            }

            html.append(Html.BUTTON.parse(HtmlUtils.getRelativeInspectUrl(name), name)).append(" ");
            i++;
        }

        html.append("</p>");
        return html.toString();
    }
}
