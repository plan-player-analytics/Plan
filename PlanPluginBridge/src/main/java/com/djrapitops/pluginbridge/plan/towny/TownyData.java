/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.towny;

import com.djrapitops.plugin.utilities.Verify;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.additional.AnalysisContainer;
import main.java.com.djrapitops.plan.data.additional.ContainerSize;
import main.java.com.djrapitops.plan.data.additional.InspectContainer;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for Towny plugin.
 *
 * @author Rsl1122
 */
public class TownyData extends PluginData {


    public TownyData() {
        super(ContainerSize.TAB, "Towny");
        super.setPluginIcon("bank");
        super.setIconColor("brown");
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) throws Exception {
        String playerName = Plan.getInstance().getDataCache().getName(uuid);

        Resident resident = TownyUniverse.getDataSource().getResident(playerName);

        if (resident.hasTown()) {
            Town town = resident.getTown();
            String townName = town.getName();
            String mayorName = town.getMayor().getName();
            String townMayor = Html.LINK.parse(Plan.getPlanAPI().getPlayerInspectPageLink(mayorName), mayorName);

            inspectContainer.addValue(getWithIcon("Town", "bank", "brown"), townName);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Mayor", "user", "brown"), townMayor);

            try {
                Coord homeBlock = town.getHomeBlock().getCoord();
                String coordinates = "x: " + homeBlock.getX() + " z: " + homeBlock.getZ();
                inspectContainer.addValue("&nbsp;" + getWithIcon("Coordinates", "map-pin", "red"), coordinates);
            } catch (TownyException e) {
            }

            int residents = town.getResidents().size();
            inspectContainer.addValue("&nbsp;" + getWithIcon("Residents", "users", "brown"), residents);
        } else {
            inspectContainer.addValue(getWithIcon("Town", "bank", "brown"), "No Town");
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) throws Exception {
        List<Town> towns = getTopTowns();

        analysisContainer.addValue(getWithIcon("Number of Towns", "bank", "brown"), towns.size());

        if (!towns.isEmpty()) {

            Map<UUID, String> userTowns = new HashMap<>();
            for (Town town : towns) {
                String townName = town.getName();
                String mayor = town.getMayor().getName();
                UUID mayorUUID = Plan.getInstance().getDataCache().getUUIDof(mayor);
                town.getResidents().stream()
                        .map(Resident::getName)
                        .map(name -> Plan.getInstance().getDataCache().getUUIDof(name))
                        .filter(Verify::notNull)
                        .forEach(uuid -> userTowns.put(uuid, uuid.equals(mayorUUID) ? "<b>" + townName + "</b>" : townName));
            }
            analysisContainer.addPlayerTableValues(getWithIcon("Town", "bank"), userTowns);
            analysisContainer.addHtml("townAccordion", TownAccordionCreator.createAccordion(towns));
        }

        return analysisContainer;
    }

    private List<Town> getTopTowns() {
        List<Town> topTowns = TownyUniverse.getDataSource().getTowns();
        topTowns.sort(new TownComparator());
        List<String> hide = Settings.HIDE_TOWNS.getStringList();
        return topTowns.stream()
                .filter(town -> !hide.contains(town.getName()))
                .collect(Collectors.toList());
    }
}