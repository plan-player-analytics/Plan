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
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.Html;
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
public class FactionsData extends PluginData {

    public FactionsData() {
        super(ContainerSize.TAB, "Factions");
        super.setPluginIcon("map");
        super.setIconColor("deep-purple");
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        MPlayer mPlayer = MPlayer.get(uuid);

        if (mPlayer.hasFaction()) {
            Faction faction = mPlayer.getFaction();
            String factionName = faction.isNone() ? "-" : faction.getName();
            double power = mPlayer.getPower();
            double maxPower = mPlayer.getPowerMax();
            String powerString = FormatUtils.cutDecimals(power) + " / " + FormatUtils.cutDecimals(maxPower);
            String factionLeader = faction.getLeader().getName();
            String factionLeaderLink = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(factionLeader), factionLeader);

            inspectContainer.addValue(getWithIcon("Faction", "flag", "deep-purple"), factionName);
            inspectContainer.addValue(getWithIcon("Power", "bolt", "purple"), powerString);
            inspectContainer.addValue(getWithIcon("Leader", "user", "purple"), factionLeaderLink);
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        List<Faction> factions = getTopFactions();

        analysisContainer.addValue(getWithIcon("Number of Factions", "flag", "deep-purple"), factions.size());

        if (!factions.isEmpty()) {
            analysisContainer.addHtml("factionAccordion", FactionAccordionCreator.createAccordion(factions));

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

            analysisContainer.addPlayerTableValues(getWithIcon("Faction", "flag"), userFactions);
        }

        return analysisContainer;
    }

    private List<Faction> getTopFactions() {
        List<Faction> topFactions = new ArrayList<>(FactionColl.get().getAll());
        topFactions.remove(FactionColl.get().getWarzone());
        topFactions.remove(FactionColl.get().getSafezone());
        topFactions.remove(FactionColl.get().getNone());
        List<String> hide = Settings.HIDE_FACTIONS.getStringList();
        return topFactions.stream()
                .filter(faction -> !hide.contains(faction.getName()))
                .sorted(new FactionComparator())
                .collect(Collectors.toList());
    }
}