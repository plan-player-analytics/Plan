/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.factions;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.additional.AnalysisContainer;
import main.java.com.djrapitops.plan.data.additional.ContainerSize;
import main.java.com.djrapitops.plan.data.additional.InspectContainer;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;

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
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) throws Exception {
        MPlayer mPlayer = MPlayer.get(uuid);

        if (mPlayer.hasFaction()) {
            Faction faction = mPlayer.getFaction();
            String factionName = faction.isNone() ? "-" : faction.getName();
            double power = mPlayer.getPower();
            double maxPower = mPlayer.getPowerMax();
            String powerString = FormatUtils.cutDecimals(power) + " / " + FormatUtils.cutDecimals(maxPower);

            inspectContainer.addValue(getWithIcon("Faction", "flag", "deep-purple"), factionName);
            inspectContainer.addValue(getWithIcon("Power", "bolt", "purple"), powerString);
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) throws Exception {
        List<Faction> factions = getTopFactions();

        analysisContainer.addValue(getWithIcon("Number of Factions", "flag", "deep-purple"), factions.size());

        if (!factions.isEmpty()) {
            analysisContainer.addHtml("factionAccordion", FactionAccordionCreator.createAccordion(factions));

            Map<UUID, String> userFactions = new HashMap<>();
            for (UUID uuid : uuids) {
                MPlayer mPlayer = MPlayer.get(uuid);

                if (mPlayer.hasFaction()) {
                    Faction faction = mPlayer.getFaction();
                    String leadername = faction.getLeader().getName();
                    String factionName = faction.isNone() ? "-" : faction.getName();

                    userFactions.put(uuid, mPlayer.getName().equals(leadername) ? "<b>" + factionName + "</b>" : factionName);
                }
            }

            analysisContainer.addPlayerTableValues(getWithIcon("Faction", "flag"), userFactions);
        }

        return analysisContainer;
    }

    public final List<Faction> getTopFactions() {
        List<Faction> topFactions = new ArrayList<>();
        topFactions.addAll(FactionColl.get().getAll());
        topFactions.remove(FactionColl.get().getWarzone());
        topFactions.remove(FactionColl.get().getSafezone());
        topFactions.remove(FactionColl.get().getNone());
        List<String> hide = Settings.HIDE_FACTIONS.getStringList();
        topFactions.sort(new FactionComparator());
        return topFactions.stream()
                .filter(faction -> !hide.contains(faction.getName()))
                .collect(Collectors.toList());
    }
}