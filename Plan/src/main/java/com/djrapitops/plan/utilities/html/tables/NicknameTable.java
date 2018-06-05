/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.utilities.FormatUtils;
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

    public NicknameTable(List<Nickname> nicknames, Map<UUID, String> serverNames) {
        super("Nickname", "Server");

        if (nicknames.isEmpty()) {
            addRow("No Nicknames");
        } else {
            addValues(nicknames, serverNames);
        }
    }

    private void addValues(List<Nickname> nicknames, Map<UUID, String> serverNames) {
        for (Nickname nickname : nicknames) {
            UUID serverUUID = nickname.getServerUUID();
            String serverName = serverNames.getOrDefault(serverUUID, "Unknown");
            long lastUsed = nickname.getLastUsed();
            addRow(
                    HtmlUtils.swapColorsToSpan(HtmlUtils.removeXSS(nickname.getName())),
                    serverName,
                    lastUsed != 0 ? FormatUtils.formatTimeStampDay(lastUsed) : "-"
            );
        }
    }
}