/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.redprotect;

import br.net.fabiozumbi12.RedProtect.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Region;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.FormatUtils;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * PluginData for RedProtect plugin.
 *
 * @author Rsl1122
 */
public class RedProtectData extends PluginData {

    public RedProtectData() {
        super(ContainerSize.THIRD, "RedProtect");
        super.setPluginIcon("map-o");
        super.setIconColor("shield");
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) throws Exception {
        Set<Region> regions = RedProtectAPI.getPlayerRegions(uuid.toString());

        inspectContainer.addValue(getWithIcon("Regions", "map-marker", "red"), regions.size());

        TableContainer regionTable = new TableContainer(
                getWithIcon("Region", "map-marker"),
                getWithIcon("World", "map"),
                getWithIcon("Area", "map-o")
        );
        long areaTotal = getTotalAndAddRows(regions, regionTable);

        inspectContainer.addValue(getWithIcon("Total Area", "map-o", "red"), areaTotal);
        inspectContainer.addTable("regionTable", regionTable);

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) throws Exception {
        Set<Region> regions = RedProtectAPI.getAllRegions();

        analysisContainer.addValue(getWithIcon("All Regions", "map-marker", "red"), regions.size());

        TableContainer regionTable = new TableContainer(
                getWithIcon("Region", "map-marker"),
                getWithIcon("World", "map"),
                getWithIcon("Area", "map-o")
        );

        long areaTotal = getTotalAndAddRows(regions, regionTable);

        analysisContainer.addValue(getWithIcon("Total Area", "map-o", "red"), areaTotal);
        analysisContainer.addTable("regionTable", regionTable);
        return analysisContainer;
    }

    private long getTotalAndAddRows(Set<Region> regions, TableContainer regionTable) {
        long areaTotal = 0;
        for (Region region : regions) {
            int area = region.getArea();
            areaTotal += area;
            String location = FormatUtils.formatLocation(region.getCenterLoc());
            String world = region.getWorld();
            regionTable.addRow(location, world, area);
        }
        return areaTotal;
    }
}