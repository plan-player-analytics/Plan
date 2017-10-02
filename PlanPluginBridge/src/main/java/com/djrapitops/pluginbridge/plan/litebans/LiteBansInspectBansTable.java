package com.djrapitops.pluginbridge.plan.litebans;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * PluginData class for LiteBans-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class LiteBansInspectBansTable extends PluginData {

    private final LiteBansDatabaseQueries db;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param database Database class for queries
     */
    public LiteBansInspectBansTable(LiteBansDatabaseQueries database) {
        super("LiteBans", "inspect_banned");
        db = database;
        String by = Html.FONT_AWESOME_ICON.parse("gavel") + " Banned By";
        String reason = Html.FONT_AWESOME_ICON.parse("balance-scale") + " Reason";
        String date = Html.FONT_AWESOME_ICON.parse("calendar-times-o") + " Expires";
        super.setPrefix(Html.TABLELINE_3.parse(date, by, reason));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        return parseContainer("", getTableLines(uuid));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }

    private String getTableLines(UUID uuid) {
        StringBuilder html = new StringBuilder();
        try {
            List<BanObject> bans = db.getBans(uuid);
            if (bans.isEmpty()) {
                html.append(Html.TABLELINE_3.parse("Not LiteBanned", "", ""));
            } else {
                for (BanObject ban : bans) {
                    long expiry = ban.getExpiry();
                    String expires = expiry <= 0 ? "Never" : FormatUtils.formatTimeStampSecond(expiry);
                    html.append(Html.TABLELINE_3_CUSTOMKEY_1.parse(
                            expiry <= 0 ? "0" : Long.toString(expiry),
                            expires,
                            Html.LINK.parse(Plan.getPlanAPI().getPlayerInspectPageLink(ban.getBannedBy()), ban.getBannedBy()),
                            ban.getReason())
                    );
                }
            }
        } catch (SQLException ex) {
            html.append(Html.TABLELINE_3.parse(ex.toString(), "", ""));
        }
        return html.toString();
    }
}
