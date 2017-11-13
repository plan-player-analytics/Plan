package com.djrapitops.pluginbridge.plan.griefprevention;

import com.djrapitops.plugin.utilities.FormatUtils;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.html.Html;
import me.ryanhamshire.GriefPrevention.DataStore;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * PluginData class for GriefPrevention-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class GriefPreventionClaimTable extends PluginData {

    private final DataStore dataStore;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param dataStore DataStore of GriefPrevention
     */
    public GriefPreventionClaimTable(DataStore dataStore) {
        super("GriefPrevention", "inspect_claim_table");
        this.dataStore = dataStore;
        String location = Html.FONT_AWESOME_ICON.parse("map-marker") + " Location";
        String size = Html.FONT_AWESOME_ICON.parse("map-o") + " Area";
        super.setPrefix(Html.TABLE_START_2.parse(location, size));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        return parseContainer(modifierPrefix, getTableLines(uuid));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }

    private String getTableLines(UUID uuid) {
        Verify.nullCheck(uuid);

        StringBuilder html = new StringBuilder();
        dataStore.getClaims().stream()
                .filter(Objects::nonNull)
                .filter(claim -> uuid.equals(claim.ownerID))
                .forEach(claim -> {
                    String location = FormatUtils.formatLocation(claim.getGreaterBoundaryCorner());
                    int area = claim.getArea();
                    html.append(Html.TABLELINE_2.parse(location, area));
                });
        return html.toString();
    }
}
