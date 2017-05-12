package main.java.com.djrapitops.plan.data.additional.towny;

import com.massivecraft.factions.entity.MPlayer;
import com.palmergames.bukkit.towny.object.Town;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 *
 * @author Rsl1122
 */
public class TownyTable extends PluginData {

    private List<Town> towns;

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
                String name = t.getName();
                String mayor = t.getMayor().getName();
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
