package com.djrapitops.pluginbridge.plan.essentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Warps;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.html.Html;

/**
 * PluginData class for Essentials-plugin.
 *
 * Registered to the plugin by EssentialsHook
 *
 * Gives a list of warps as a String value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see EssentialsHook
 */
public class EssentialsWarps extends PluginData {

    private final Essentials essentials;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param essentials Instance of Essentials plugin.
     */
    public EssentialsWarps(Essentials essentials) {
        super("Essentials", "warps", AnalysisType.HTML);
        this.essentials = essentials;
        String warps = Html.FONT_AWESOME_ICON.parse("map-marker") + " Warps";
        String command = Html.FONT_AWESOME_ICON.parse("fa-terminal") + " Command";
        super.setPrefix(Html.TABLE_START_2.parse(warps, command));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifier, UUID uuid) {
        Warps warps = essentials.getWarps();
        if (!warps.isEmpty()) {
            Collection<String> warpList = warps.getList();

            return parseContainer("", getTableContents(new ArrayList<>(warpList)));
        }
        return parseContainer("", Html.TABLELINE_2.parse("No Warps.", ""));
    }

    private String getTableContents(List<String> warps) {
        Collections.sort(warps);
        StringBuilder html = new StringBuilder();
        if (warps.isEmpty()) {
            html.append(Html.TABLELINE_4.parse(Html.FACTION_NO_FACTIONS.parse(), "", "", ""));
        } else {
            for (String warp : warps) {
                html.append(Html.TABLELINE_2.parse(warp, "/warp " + warp));
            }
        }
        return html.toString();
    }

    @Override
    public Serializable getValue(UUID uuid) {
        Warps warps = essentials.getWarps();
        if (!warps.isEmpty()) {
            return warps.getList().toString();
        }
        return "No Warps.";
    }

}
