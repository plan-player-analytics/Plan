/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html.tables;

import main.java.com.djrapitops.plan.utilities.html.Html;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility Class for creating Actions Table for inspect page.
 *
 * @author Rsl1122
 */
public class NicknameTableCreator {


    public NicknameTableCreator() {
        throw new IllegalStateException("Utility class");
    }

    public static String createTable(Map<UUID, List<String>> nicknames, Map<UUID, String> serverNames) {
        StringBuilder html = new StringBuilder();
        if (nicknames.isEmpty()) {
            html.append(Html.TABLELINE_2.parse("No Nicknames", "-"));
        } else {
            for (Map.Entry<UUID, List<String>> entry : nicknames.entrySet()) {
                String serverName = serverNames.getOrDefault(entry.getKey(), "Unknown");
                for (String nick : entry.getValue()) {
                    html.append(Html.TABLELINE_2.parse(nick, serverName));
                }
            }
        }
        return html.toString();
    }
}