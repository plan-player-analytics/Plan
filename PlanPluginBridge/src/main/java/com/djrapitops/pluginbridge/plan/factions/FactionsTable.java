package com.djrapitops.pluginbridge.plan.factions;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 * PluginData class for Factions-plugin.
 *
 * Registered to the plugin by FactionsHook
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see FactionsHook
 */
public class FactionsTable extends PluginData {

    private List<Faction> factions;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * Uses Html to easily parse Html for the table.
     *
     * @see FactionsHook
     * @see Html
     */
    public FactionsTable() {
        super("Factions", "faction_stable", AnalysisType.HTML);
        this.factions = getTopFactions();
        super.setPrefix(Html.TABLE_FACTIONS_START.parse());
        super.setSuffix(Html.TABLE_END.parse());
    }

    /**
     * Used to get the list of Factions and filter out unnecessary ones.
     *
     * @return List of Factions sorted by power
     */
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

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        StringBuilder html = new StringBuilder();
        this.factions = getTopFactions();
        if (factions.isEmpty()) {
            html.append(Html.TABLELINE_4.parse(Html.FACTION_NO_FACTIONS.parse(), "", "", ""));
        } else {
            for (Faction f : factions) {
                String name;
                String leader;
                String power;
                String land;
                if (f != null) {
                    name = f.getName();
                    MPlayer fLeader = f.getLeader();
                    leader = fLeader != null ? fLeader.getNameAndSomething("", "") : Html.FACTION_NO_LEADER.parse();
                    power = FormatUtils.cutDecimals(f.getPower());
                    land = f.getLandCount() + "";
                } else {
                    name = Html.FACTION_NOT_FOUND.parse();
                    leader = Html.FACTION_NOT_FOUND.parse();
                    power = Html.FACTION_NOT_FOUND.parse();
                    land = Html.FACTION_NOT_FOUND.parse();
                }
                String leaderPage = Html.LINK.parse(HtmlUtils.getInspectUrl(leader), leader);
                html.append(Html.TABLELINE_4.parse(name, power, land, leaderPage));
            }
        }
        return parseContainer(modifierPrefix, html.toString());
    }

    @Override
    public Serializable getValue(UUID uuid) {
        MPlayer mPlayer = MPlayer.get(uuid);
        return mPlayer.getPower();
    }
}
