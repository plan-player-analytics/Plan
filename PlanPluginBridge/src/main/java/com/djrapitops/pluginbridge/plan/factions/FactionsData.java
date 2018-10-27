/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.factions;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
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
        List<String> hide = config.getStringList(Settings.HIDE_FACTIONS);
        return topFactions.stream()
                .filter(faction -> !hide.contains(faction.getName()))
                .sorted(new FactionComparator())
                .collect(Collectors.toList());
    }
}