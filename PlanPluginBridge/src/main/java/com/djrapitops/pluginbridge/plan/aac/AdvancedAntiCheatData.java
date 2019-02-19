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
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plugin.utilities.Format;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PluginData for AAC plugin.
 *
 * @author Rsl1122
 */
class AdvancedAntiCheatData extends PluginData {

    private final Database database;
    private final Formatter<Long> timestampFormatter;

    AdvancedAntiCheatData(Database database, Formatter<Long> timestampFormatter) {
        super(ContainerSize.THIRD, "AdvancedAntiCheat");
        this.timestampFormatter = timestampFormatter;
        super.setPluginIcon(Icon.called("heart").of(Color.RED).build());
        this.database = database;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        List<HackObject> hackObjects = database.query(HackerTable.getHackObjects(uuid));

        inspectContainer.addValue(
                getWithIcon("Times Kicked for Possible Hacking", Icon.called("exclamation-triangle").of(Color.RED)),
                hackObjects.size()
        );

        TableContainer hackTable = new TableContainer(
                getWithIcon("Kicked", Icon.called("calendar").of(Family.REGULAR)),
                getWithIcon("Hack", Icon.called("exclamation-triangle")),
                getWithIcon("Violation Level", Icon.called("gavel"))
        );
        hackTable.setColor("red");

        for (HackObject hackObject : hackObjects) {
            String date = timestampFormatter.apply(hackObject.getDate());
            String hack = new Format(hackObject.getHackType()).capitalize().toString();
            hackTable.addRow(date, hack, hackObject.getViolationLevel());
        }
        inspectContainer.addTable("hackTable", hackTable);

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        Map<UUID, List<HackObject>> hackObjects = database.query(HackerTable.getHackObjects());

        Map<UUID, Integer> violations = hackObjects.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

        analysisContainer.addPlayerTableValues(getWithIcon("Kicked for Hacking", Icon.called("exclamation-triangle")), violations);

        return analysisContainer;
    }
}