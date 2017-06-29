package com.djrapitops.pluginbridge.plan.towny;

import com.massivecraft.factions.entity.MPlayer;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 * PluginData class for Towny-plugin.
 *
 * Registered to the plugin by TownyHook
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see TownyHook
 */
public class TownyTable extends PluginData {

    private List<Town> towns;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * Uses Html to easily parse Html for the table.
     *
     * @see TownyHook
     * @see Html
     */
    public TownyTable() {
        super("Towny", "townstable", AnalysisType.HTML);
        super.setPrefix(Html.TABLE_TOWNS_START.parse());
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        StringBuilder html = new StringBuilder();
        this.towns = getTopTowns();
        if (towns.isEmpty()) {
            html.append(Html.TABLELINE_4.parse(Html.TOWN_NO_TOWNS.parse(), "", "", ""));
        } else {
            for (Town t : towns) {
                if (t == null) {
                    continue;
                }
                String name = t.getName();
                Resident mayorR = t.getMayor();
                String mayor;
                if (mayorR != null) {
                    mayor = mayorR.getName();
                } else {
                    mayor = "None";
                }
                String residents = t.getNumResidents() + "";
                String land = t.getPurchasedBlocks() + "";
                String leaderPage = Html.LINK.parse(HtmlUtils.getInspectUrl(mayor), mayor);
                html.append(Html.TABLELINE_4.parse(name, residents, land, leaderPage));
            }
        }
        return parseContainer(modifierPrefix, html.toString());
    }

    /**
     * Used to get the list of Towns and filter out unnessecary ones.
     *
     * @return List of Towns sorted by amount of residents.
     */
    public List<Town> getTopTowns() {
        List<Town> topTowns = TownyUniverse.getDataSource().getTowns();
        Collections.sort(topTowns, new TownComparator());
        List<String> hide = Settings.HIDE_TOWNS.getStringList();
        List<Town> townNames = topTowns.stream()
                .filter(town -> !hide.contains(town.getName()))
                .collect(Collectors.toList());
        return townNames;
    }

    @Override
    public Serializable getValue(UUID uuid) {
        MPlayer mPlayer = MPlayer.get(uuid);
        return mPlayer.getPower();
    }
}
