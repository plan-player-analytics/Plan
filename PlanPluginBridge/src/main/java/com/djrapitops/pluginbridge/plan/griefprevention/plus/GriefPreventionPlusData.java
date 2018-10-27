/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.griefprevention.plus;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.DataStore;
import org.bukkit.Location;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for GriefPreventionPlus plugin.
 *
 * @author Rsl1122
 */
class GriefPreventionPlusData extends PluginData {

    private final DataStore dataStore;

    GriefPreventionPlusData(DataStore dataStore) {
        super(ContainerSize.THIRD, "GriefPreventionPlus");
        setPluginIcon(Icon.called("shield-alt").of(Color.BLUE_GREY).build());
        this.dataStore = dataStore;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        Map<String, Integer> claims = dataStore.getClaims().values().stream()
                .filter(Objects::nonNull)
                .filter(claim -> uuid.equals(claim.getOwnerID()))
                .collect(Collectors.toMap(
                        claim -> formatLocation(claim.getGreaterBoundaryCorner()),
                        Claim::getArea)
                );
        long totalArea = claims.values().stream().mapToInt(i -> i).sum();

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

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        Map<UUID, Integer> area = new HashMap<>();

        for (Claim claim : dataStore.getClaims().values()) {
            if (claim == null) {
                continue;
            }
            UUID uuid = claim.getOwnerID();
            int blocks = area.getOrDefault(uuid, 0);
            blocks += claim.getArea();
            area.put(uuid, blocks);
        }

        long totalArea = area.values().stream().mapToInt(i -> i).sum();
        analysisContainer.addValue(getWithIcon("Total Claimed Area", Icon.called("map").of(Family.REGULAR).of(Color.BLUE_GREY)), totalArea);

        analysisContainer.addPlayerTableValues(getWithIcon("Claimed Area", Icon.called("map").of(Family.REGULAR)), area);

        return analysisContainer;
    }

    private String formatLocation(Location greaterBoundaryCorner) {
        return "x: " + greaterBoundaryCorner.getBlockX() + " z: " + greaterBoundaryCorner.getBlockZ();
    }
}