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
package com.djrapitops.pluginbridge.plan.factions;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.PluginDataSettings;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for Factions plugin.
 *
 * @author Rsl1122
 */
class FactionsData extends PluginData {

    private final PlanConfig config;
    private final Formatter<Long> timestampFormatter;
    private final Formatter<Double> decimalFormatter;

    FactionsData(
            PlanConfig config,
            Formatter<Long> timestampFormatter,
            Formatter<Double> decimalFormatter
    ) {
        super(ContainerSize.TAB, "Factions");
        this.config = config;
        this.timestampFormatter = timestampFormatter;
        this.decimalFormatter = decimalFormatter;
        setPluginIcon(Icon.called("map").of(Color.DEEP_PURPLE).build());
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        MPlayer mPlayer = MPlayer.get(uuid);

        if (mPlayer == null) {
            return inspectContainer;
        }

        if (mPlayer.hasFaction()) {
            Faction faction = mPlayer.getFaction();
            if (faction != null) {
                String factionName = faction.isNone() ? "-" : faction.getName();
                String factionLeader = faction.getLeader().getName();
                String factionLeaderLink = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(factionLeader), factionLeader);

                inspectContainer.addValue(getWithIcon("Faction", Icon.called("flag").of(Color.DEEP_PURPLE)), factionName);
                inspectContainer.addValue(getWithIcon("Leader", Icon.called("user").of(Color.PURPLE)), factionLeaderLink);
            }
        }

        double power = mPlayer.getPower();
        double maxPower = mPlayer.getPowerMax();
        String powerString = decimalFormatter.apply(power) + " / " + decimalFormatter.apply(maxPower);
        inspectContainer.addValue(getWithIcon("Power", Icon.called("bolt").of(Color.PURPLE)), powerString);

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        List<Faction> factions = getTopFactions();

        analysisContainer.addValue(getWithIcon("Number of Factions", Icon.called("flag").of(Color.DEEP_PURPLE)), factions.size());

        if (!factions.isEmpty()) {
            FactionsAccordion factionsAccordion = new FactionsAccordion(
                    factions,
                    Optional.ofNullable(analysisData).flatMap(c -> c.getValue(AnalysisKeys.PLAYERS_MUTATOR))
                            .orElse(new PlayersMutator(new ArrayList<>())),
                    timestampFormatter, decimalFormatter);
            analysisContainer.addHtml("factionAccordion", factionsAccordion.toHtml());

            Map<UUID, String> userFactions = new HashMap<>();
            for (UUID uuid : uuids) {
                MPlayer mPlayer = MPlayer.get(uuid);

                if (mPlayer.hasFaction()) {
                    Faction faction = mPlayer.getFaction();
                    if (faction == null) {
                        continue;
                    }
                    MPlayer leader = faction.getLeader();
                    String leaderName = leader != null ? leader.getName() : "";
                    String factionName = faction.isNone() ? "-" : faction.getName();

                    userFactions.put(uuid, mPlayer.getName().equals(leaderName) ? "<b>" + factionName + "</b>" : factionName);
                }
            }

            analysisContainer.addPlayerTableValues(getWithIcon("Faction", Icon.called("flag")), userFactions);
        }

        return analysisContainer;
    }

    private List<Faction> getTopFactions() {
        List<Faction> topFactions = new ArrayList<>(FactionColl.get().getAll());
        topFactions.remove(FactionColl.get().getWarzone());
        topFactions.remove(FactionColl.get().getSafezone());
        topFactions.remove(FactionColl.get().getNone());
        List<String> hide = config.get(PluginDataSettings.HIDE_FACTIONS);
        return topFactions.stream()
                .filter(faction -> !hide.contains(faction.getName()))
                .sorted(new FactionComparator())
                .collect(Collectors.toList());
    }
}