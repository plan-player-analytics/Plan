package main.java.com.djrapitops.plan.data.additional.factions;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 *
 * @author Rsl1122
 */
public class FactionsTable extends PluginData {

    private List<Faction> factions;

    public FactionsTable(List<Faction> factions) {
        super("Factions", "factionstable", AnalysisType.HTML);
        this.factions = factions;
        super.setPrefix(Html.TABLE_FACTIONS_START.parse());
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        StringBuilder html = new StringBuilder();
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
