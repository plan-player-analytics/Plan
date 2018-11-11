/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
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
