/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.HtmlUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Html table that displays player's nicknames and where they were seen.
 *
 * @author Rsl1122
 */
class NicknameTable extends TableContainer {

    private final Formatter<DateHolder> yearFormatter;

    NicknameTable(List<Nickname> nicknames, Map<UUID, String> serverNames, Formatter<DateHolder> yearFormatter) {
        super("Nickname", "Server", "Last Seen");
        this.yearFormatter = yearFormatter;

        if (nicknames.isEmpty()) {
            addRow("No Nicknames");
        } else {
            addValues(nicknames, serverNames);
        }
    }

    private void addValues(List<Nickname> nicknames, Map<UUID, String> serverNames) {
        nicknames.sort(new DateHolderRecentComparator());

        for (Nickname nickname : nicknames) {
            UUID serverUUID = nickname.getServerUUID();
            String serverName = serverNames.getOrDefault(serverUUID, "Unknown");
            addRow(
                    HtmlUtils.swapColorsToSpan(HtmlUtils.removeXSS(nickname.getName())),
                    serverName,
                    yearFormatter.apply(nickname)
            );
        }
    }
}
