package com.djrapitops.pluginbridge.plan.litebans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 * PluginData class for LiteBans-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class LiteBansBansTable extends PluginData {

    private final LiteBansDatabaseQueries db;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param database Database class for queries
     */
    public LiteBansBansTable(LiteBansDatabaseQueries database) {
        super("LiteBans", "bantable", AnalysisType.HTML);
        db = database;
        String banned = Html.FONT_AWESOME_ICON.parse("ban") + " Banned";
        String by = Html.FONT_AWESOME_ICON.parse("gavel") + " Banned By";
        String reason = Html.FONT_AWESOME_ICON.parse("balance-scale") + " Reason";
        String date = Html.FONT_AWESOME_ICON.parse("calendar-times-o") + " Expires";
        super.setPrefix(Html.TABLELINE_4.parse(banned, by, reason, date));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        return parseContainer("", getTableLines());
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }

    private String getTableLines() {
        StringBuilder html = new StringBuilder();
        try {
            List<BanObject> bans = db.getBans();
            Map<UUID, UserData> users = Plan.getPlanAPI().getInspectCachedUserDataMap();
            for (BanObject ban : bans) {
                UUID uuid = ban.getUuid();
                UserData userData = users.get(uuid);
                if (userData == null) {
                    continue;
                }
                String name = userData.getName();
                String tableLine = "<tr><td>REPLACE0</td><td>REPLACE1</td><td>REPLACE2</td><td sorttable_customkey=\"REPLACE3\">REPLACE4</td></tr>";
                long expiry = ban.getExpiry();
                String expires = expiry <= 0 ? "Never" : FormatUtils.formatTimeStampSecond(expiry);
                html.append(tableLine
                        .replace("REPLACE0", Html.LINK.parse(HtmlUtils.getInspectUrl(name), name))
                        .replace("REPLACE1", Html.LINK.parse(HtmlUtils.getInspectUrl(ban.getBannedBy()), ban.getBannedBy()))
                        .replace("REPLACE2", ban.getReason())
                        .replace("REPLACE3", expiry <= 0 ? "0" : expiry + "")
                        .replace("REPLACE4", expires
                        )
                );
            }
        } catch (SQLException ex) {
            html.append(Html.TABLELINE_4.parse(ex.toString(), "", "", ""));
        }
        return html.toString();
    }
}
