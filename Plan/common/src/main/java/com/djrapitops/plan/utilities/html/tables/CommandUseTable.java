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
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Html table that displays how many times each command is used.
 *
 * @author Rsl1122
 */
class CommandUseTable extends TableContainer {

    CommandUseTable(DataContainer container) {
        super(Icon.called("terminal") + " Command", Icon.called("list-ol") + "Times Used");

        Map<String, Integer> commandUse = container.getValue(ServerKeys.COMMAND_USAGE).orElse(new HashMap<>());

        setColor("lime");
        if (commandUse.isEmpty()) {
            addRow("No Commands");
        } else {
            addValues(commandUse);
        }
    }

    private List<Map.Entry<String, Integer>> sortByValue(Map<String, Integer> map) {
        return map.entrySet().stream()
                .sorted((one, two) -> Integer.compare(two.getValue(), one.getValue()))
                .collect(Collectors.toList());
    }

    private void addValues(Map<String, Integer> commandUse) {
        List<Map.Entry<String, Integer>> sorted = sortByValue(commandUse);

        int i = 0;
        for (Map.Entry<String, Integer> entry : sorted) {
            if (i >= 500) {
                break;
            }
            String command = HtmlUtils.removeXSS(entry.getKey());
            addRow(command, entry.getValue());

            i++;
        }
    }
}
