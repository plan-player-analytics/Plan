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
package com.djrapitops.pluginbridge.plan.redprotect;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * PluginData for RedProtect plugin.
 *
 * @author Rsl1122
 */
class RedProtectData extends PluginData {

    RedProtectData() {
        super(ContainerSize.THIRD, "RedProtect");
        setPluginIcon(Icon.called("shield-alt").of(Color.RED).build());
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        Set<Region> regions = RedProtect.get().getAPI().getPlayerRegions(uuid.toString());

        addRegionData(inspectContainer, regions);

        return inspectContainer;
    }

    private void addRegionData(InspectContainer inspectContainer, Set<Region> regions) {
        inspectContainer.addValue(getWithIcon("Regions", Icon.called("map-marker").of(Color.RED)), regions.size());

        TableContainer regionTable = new TableContainer(
                getWithIcon("Region", Icon.called("map-marker")),
                getWithIcon("World", Icon.called("map")),
                getWithIcon("Area", Icon.called("map").of(Family.REGULAR))
        );
        long areaTotal = getTotalAndAddRows(regions, regionTable);

        inspectContainer.addValue(getWithIcon("Total Area", Icon.called("map").of(Family.REGULAR).of(Color.RED)), areaTotal);
        inspectContainer.addTable("regionTable", regionTable);
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        Set<Region> regions = RedProtect.get().getAPI().getAllRegions();

        addRegionData(analysisContainer, regions);
        return analysisContainer;
    }

    private long getTotalAndAddRows(Set<Region> regions, TableContainer regionTable) {
        long areaTotal = 0;
        for (Region region : regions) {
            int area = region.getArea();
            areaTotal += area;
            Location center = region.getCenterLoc();
            String location = "x: " + center.getBlockX() + ", z: " + center.getBlockZ();
            String world = region.getWorld();
            regionTable.addRow(location, world, area);
        }
        return areaTotal;
    }
}