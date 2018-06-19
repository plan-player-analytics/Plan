/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.utilities.html.HtmlUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility Class for creating Nicknames Table for inspect page.
 *
 * @author Rsl1122
 */
public class NicknameTable extends TableContainer {

    public NicknameTable(Map<UUID, List<String>> nicknames, Map<UUID, String> serverNames) {
        super("Nickname", "Server");

        if (nicknames.isEmpty()) {
            addRow("No Nicknames");
        } else {
            addValues(nicknames, serverNames);
        }
    }

    private void addValues(Map<UUID, List<String>> nicknames, Map<UUID, String> serverNames) {
        for (Map.Entry<UUID, List<String>> entry : nicknames.entrySet()) {
            String serverName = serverNames.getOrDefault(entry.getKey(), "Unknown");
            for (String nick : entry.getValue()) {
                addRow(
                        HtmlUtils.swapColorsToSpan(HtmlUtils.removeXSS(nick)),
                        serverName
                );
            }
        }
    }
}
