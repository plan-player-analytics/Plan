/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plugin.utilities.Format;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.FormatUtils;

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
public class AdvancedAntiCheatData extends PluginData {

    private final HackerTable table;

    public AdvancedAntiCheatData(HackerTable table) {
        super(ContainerSize.THIRD, "AdvancedAntiCheat");
        super.setPluginIcon("heart");
        super.setIconColor("red");
        this.table = table;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) throws Exception {
        List<HackObject> hackObjects = table.getHackObjects(uuid);

        inspectContainer.addValue(getWithIcon("Times Kicked for Possible Hacking", "exclamation-triangle", "red"), hackObjects.size());

        TableContainer hackTable = new TableContainer(
                getWithIcon("Kicked", "calendar"),
                getWithIcon("Hack", "exclamation-triangle"),
                getWithIcon("Violation Level", "gavel")
        );
        hackTable.setColor("red");

        for (HackObject hackObject : hackObjects) {
            String date = FormatUtils.formatTimeStampYear(hackObject.getDate());
            String hack = Format.create(hackObject.getHackType().getName()).capitalize().toString();
            hackTable.addRow(date, hack, hackObject.getViolationLevel());
        }
        inspectContainer.addTable("hackTable", hackTable);

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) throws Exception {
        Map<UUID, List<HackObject>> hackObjects = table.getHackObjects();

        Map<UUID, Integer> violations = hackObjects.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

        analysisContainer.addPlayerTableValues(getWithIcon("Kicked for Hacking", "exclamation-triangle"), violations);

        return analysisContainer;
    }
}