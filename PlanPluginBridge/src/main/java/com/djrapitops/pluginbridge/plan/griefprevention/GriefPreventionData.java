/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.griefprevention;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plugin.utilities.FormatUtils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for GriefPrevention plugin.
 *
 * @author Rsl1122
 */
public class GriefPreventionData extends PluginData {

    private final DataStore dataStore;

    public GriefPreventionData(DataStore dataStore) {
        super(ContainerSize.THIRD, "GriefPrevention");
        super.setPluginIcon("shield");
        super.setIconColor("blue-grey");
        this.dataStore = dataStore;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        Map<String, Integer> claims = dataStore.getClaims().stream()
                .filter(Objects::nonNull)
                .filter(claim -> uuid.equals(claim.ownerID))
                .collect(Collectors.toMap(
                        claim -> FormatUtils.formatLocation(claim.getGreaterBoundaryCorner()),
                        Claim::getArea)
                );
        String softMuted = dataStore.isSoftMuted(uuid) ? "Yes" : "No";
        long totalArea = claims.values().stream().mapToInt(i -> i).sum();

        inspectContainer.addValue(getWithIcon("SoftMuted", "bell-slash-o", "deep-orange"), softMuted);
        inspectContainer.addValue(getWithIcon("Claims", "map-marker", "blue-grey"), claims.size());
        inspectContainer.addValue(getWithIcon("Claimed Area", "map-o", "light-green"), totalArea);

        TableContainer claimsTable = new TableContainer(getWithIcon("Claim", "map-marker"), getWithIcon("Area", "map-o"));
        claimsTable.setColor("blue-grey");
        for (Map.Entry<String, Integer> entry : claims.entrySet()) {
            claimsTable.addRow(entry.getKey(), entry.getValue());
        }
        inspectContainer.addTable("claimTable", claimsTable);

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        Map<UUID, Integer> area = new HashMap<>();

        for (Claim claim : dataStore.getClaims()) {
            if (claim == null) {
                continue;
            }
            UUID uuid = claim.ownerID;
            int blocks = area.getOrDefault(uuid, 0);
            blocks += claim.getArea();
            area.put(uuid, blocks);
        }

        long totalArea = area.values().stream().mapToInt(i -> i).sum();
        analysisContainer.addValue(getWithIcon("Total Claimed Area", "map-o", "blue-grey"), totalArea);

        analysisContainer.addPlayerTableValues(getWithIcon("Claimed Area", "map-o"), area);

        return analysisContainer;
    }
}