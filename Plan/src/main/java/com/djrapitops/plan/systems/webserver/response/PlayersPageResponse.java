package main.java.com.djrapitops.plan.systems.webserver.response;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.systems.webserver.theme.Theme;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import main.java.com.djrapitops.plan.utilities.html.Html;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PlayersPageResponse extends Response {

    public PlayersPageResponse() {
        super.setHeader("HTTP/1.1 200 OK");
        try {
            IPlan plugin = MiscUtils.getIPlan();
            List<String> names = new ArrayList<>(plugin.getDB().getUsersTable().getPlayerNames().values());
            Collections.sort(names);
            Map<String, String> replace = new HashMap<>();
            replace.put("content", buildContent(names));
            replace.put("version", plugin.getVersion());
            super.setContent(Theme.replaceColors(StrSubstitutor.replace(FileUtil.getStringFromResource("players.html"), replace)));
        } catch (SQLException | FileNotFoundException e) {
            Log.toLog(this.getClass().getName(), e);
            setContent(new InternalErrorResponse(e, "/players").getContent());
        }
    }

    public static String buildContent(List<String> names) {
        StringBuilder html = new StringBuilder("<p>");
        int size = names.size();

        html.append(size).append(" players. Use browser's Search to find players by name. (Ctrl+F)</p><table><tr>");

        int i = 1;
        for (String name : names) {
            String link = Html.LINK.parse("../player/" + name, name);

            html.append("<td>").append(link).append("</td>");

            if (i < size && i % 8 == 0) {
                html.append("</tr><tr>");
            }
            i++;
        }

        html.append("</tr></table>");
        return html.toString();
    }
}
