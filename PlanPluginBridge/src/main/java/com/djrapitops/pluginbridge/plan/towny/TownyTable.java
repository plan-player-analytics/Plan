package com.djrapitops.pluginbridge.plan.towny;

import com.massivecraft.factions.entity.MPlayer;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
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
     * @param towns List of filtered Towns.
     * @see TownyHook
     * @see Html
     */
    public TownyTable(List<Town> towns) {
        super("Towny", "townstable", AnalysisType.HTML);
        this.towns = towns;
        super.setPrefix(Html.TABLE_TOWNS_START.parse());
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        StringBuilder html = new StringBuilder();
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

    @Override
    public Serializable getValue(UUID uuid) {
        MPlayer mPlayer = MPlayer.get(uuid);
        return mPlayer.getPower();
    }
}
