/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
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
        super("Nickname", "Server", "Last Seen");

        if (nicknames.isEmpty()) {
            addRow("No Nicknames");
        } else {
            addValues(nicknames, serverNames);
        }
    }

    private void addValues(List<Nickname> nicknames, Map<UUID, String> serverNames) {
        nicknames.sort(new DateHolderRecentComparator());

        Formatter<DateHolder> formatter = Formatters.year();
        for (Nickname nickname : nicknames) {
            UUID serverUUID = nickname.getServerUUID();
            String serverName = serverNames.getOrDefault(serverUUID, "Unknown");
            addRow(
                    HtmlUtils.swapColorsToSpan(HtmlUtils.removeXSS(nickname.getName())),
                    serverName,
                    formatter.apply(nickname)
            );
        }
    }
}
