/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.towny;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plugin.utilities.Verify;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for Towny plugin.
 *
 * @author Rsl1122
 */
class TownyData extends PluginData {

    private final PlanConfig config;
    private final DataCache dataCache;

    TownyData(PlanConfig config, DataCache dataCache) {
        super(ContainerSize.TAB, "Towny");
        this.config = config;
        this.dataCache = dataCache;
        setPluginIcon(Icon.called("university").of(Color.BROWN).build());
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        String playerName = dataCache.getName(uuid);

        try {
            Resident resident = TownyUniverse.getDataSource().getResident(playerName);

            if (resident.hasTown()) {
                Town town = resident.getTown();
                String townName = town.getName();
                String mayorName = town.getMayor().getName();
                String townMayor = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(mayorName), mayorName);

                inspectContainer.addValue(getWithIcon("Town", Icon.called("university").of(Color.BROWN)), townName);
                inspectContainer.addValue(getWithIcon("Town Mayor", Icon.called("user").of(Color.BROWN)), townMayor);

                try {
                    Coord homeBlock = town.getHomeBlock().getCoord();
                    String coordinates = "x: " + homeBlock.getX() + " z: " + homeBlock.getZ();
                    inspectContainer.addValue(getWithIcon("Town Coordinates", Icon.called("map-pin").of(Color.RED)), coordinates);
                } catch (TownyException ignore) {
                    /* Town has no home block */
                }

                int residents = town.getResidents().size();
                inspectContainer.addValue(getWithIcon("Town Residents", Icon.called("users").of(Color.BROWN)), residents);
                return inspectContainer;
            }
        } catch (NotRegisteredException ignore) {
            /* No Towny Resident. */
        }
        inspectContainer.addValue(getWithIcon("Town", Icon.called("bank").of(Color.BROWN)), "No Town");
        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        List<Town> towns = getTopTowns();

        analysisContainer.addValue(getWithIcon("Number of Towns", Icon.called("university").of(Color.BROWN)), towns.size());

        if (!towns.isEmpty()) {

            Map<UUID, String> userTowns = new HashMap<>();
            for (Town town : towns) {
                String townName = town.getName();
                String mayor = town.getMayor().getName();
                UUID mayorUUID = dataCache.getUUIDof(mayor);
                town.getResidents().stream()
                        .map(Resident::getName)
                        .map(dataCache::getUUIDof)
                        .filter(Verify::notNull)
                        .forEach(uuid -> userTowns.put(uuid, uuid.equals(mayorUUID) ? "<b>" + townName + "</b>" : townName));
            }
            analysisContainer.addPlayerTableValues(getWithIcon("Town", Icon.called("university")), userTowns);

            TownsAccordion townsAccordion = new TownsAccordion(
                    towns,
                    Optional.ofNullable(analysisData).flatMap(c -> c.getValue(AnalysisKeys.PLAYERS_MUTATOR))
                            .orElse(new PlayersMutator(new ArrayList<>()))
            );

            analysisContainer.addHtml("townAccordion", townsAccordion.toHtml());
        }

        return analysisContainer;
    }

    private List<Town> getTopTowns() {
        List<Town> topTowns = TownyUniverse.getDataSource().getTowns();
        topTowns.sort(new TownComparator());
        List<String> hide = config.getStringList(Settings.HIDE_TOWNS);
        return topTowns.stream()
                .filter(town -> !hide.contains(town.getName()))
                .collect(Collectors.toList());
    }
}