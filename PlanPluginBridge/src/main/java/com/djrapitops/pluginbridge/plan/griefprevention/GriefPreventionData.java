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
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import org.bukkit.Location;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for GriefPrevention plugin.
 *
 * @author Rsl1122
 */
class GriefPreventionData extends PluginData {

    private final DataStore dataStore;

    GriefPreventionData(DataStore dataStore) {
        super(ContainerSize.THIRD, "GriefPrevention");
        setPluginIcon(Icon.called("shield-alt").of(Color.BLUE_GREY).build());
        this.dataStore = dataStore;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        Map<String, Integer> claims = dataStore.getClaims().stream()
                .filter(Objects::nonNull)
                .filter(claim -> uuid.equals(claim.ownerID))
                .collect(Collectors.toMap(
                        claim -> formatLocation(claim.getGreaterBoundaryCorner()),
                        Claim::getArea)
                );
        String softMuted = dataStore.isSoftMuted(uuid) ? "Yes" : "No";
        long totalArea = claims.values().stream().mapToInt(i -> i).sum();

        inspectContainer.addValue(getWithIcon("SoftMuted", Icon.called("bell-slash").of(Color.DEEP_ORANGE).of(Family.REGULAR)), softMuted);
        inspectContainer.addValue(getWithIcon("Claims", Icon.called("map-marker").of(Color.BLUE_GREY)), claims.size());
        inspectContainer.addValue(getWithIcon("Claimed Area", Icon.called("map").of(Family.REGULAR).of(Color.BLUE_GREY)), totalArea);

        TableContainer claimsTable = new TableContainer(
                getWithIcon("Claim", Icon.called("map-marker")),
                getWithIcon("Area", Icon.called("map").of(Family.REGULAR))
        );
        claimsTable.setColor("blue-grey");
        for (Map.Entry<String, Integer> entry : claims.entrySet()) {
            claimsTable.addRow(entry.getKey(), entry.getValue());
        }
        inspectContainer.addTable("claimTable", claimsTable);

        return inspectContainer;
    }

    private String formatLocation(Location greaterBoundaryCorner) {
        return "x: " + greaterBoundaryCorner.getBlockX() + " z: " + greaterBoundaryCorner.getBlockZ();
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
        analysisContainer.addValue(getWithIcon("Total Claimed Area", Icon.called("map").of(Family.REGULAR).of(Color.BLUE_GREY)), totalArea);

        analysisContainer.addPlayerTableValues(getWithIcon("Claimed Area", Icon.called("map").of(Family.REGULAR)), area);

        return analysisContainer;
    }
}