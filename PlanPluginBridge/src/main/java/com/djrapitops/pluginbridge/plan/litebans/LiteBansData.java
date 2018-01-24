/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.BanData;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for LiteBans plugin.
 *
 * @author Rsl1122
 */
public class LiteBansData extends PluginData implements BanData {

    private final LiteBansDatabaseQueries db;

    public LiteBansData(LiteBansDatabaseQueries db) {
        super(ContainerSize.TWO_THIRDS, "LiteBans");
        super.setIconColor("red");
        super.setPluginIcon("ban");
        this.db = db;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {

        String by = getWithIcon("Banned By", "gavel");
        String reason = getWithIcon("Reason", "balance-scale");
        String date = getWithIcon("Expires", "calendar-times-o");
        TableContainer banTable = new TableContainer(date, by, reason);
        banTable.setColor("red");

        try {
            List<BanObject> bans = db.getBans(uuid);
            if (bans.isEmpty()) {
                banTable.addRow("Not LiteBanned");
            } else {
                for (BanObject ban : bans) {
                    long expiry = ban.getExpiry();
                    String expires = expiry <= 0 ? "Never" : FormatUtils.formatTimeStampSecond(expiry);
                    banTable.addRow(
                            expires,
                            Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(ban.getBannedBy()), ban.getBannedBy()),
                            ban.getReason()
                    );
                }
            }
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            banTable.addRow("Error: " + ex);
        }
        inspectContainer.addTable("banTable", banTable);

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) throws Exception {
        String banned = Html.FONT_AWESOME_ICON.parse("ban") + " Banned";
        String by = Html.FONT_AWESOME_ICON.parse("gavel") + " Banned By";
        String reason = Html.FONT_AWESOME_ICON.parse("balance-scale") + " Reason";
        String date = Html.FONT_AWESOME_ICON.parse("calendar-times-o") + " Expires";
        TableContainer banTable = new TableContainer(banned, by, reason, date);
        banTable.setColor("red");

        List<BanObject> bans = db.getBans();
        for (BanObject ban : bans) {
            UUID uuid = ban.getUuid();
            String name = DataCache.getInstance().getName(uuid);
            long expiry = ban.getExpiry();
            String expires = expiry <= 0 ? "Never" : FormatUtils.formatTimeStampSecond(expiry);

            banTable.addRow(
                    Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(name), name),
                    Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(ban.getBannedBy()), ban.getBannedBy()),
                    ban.getReason(),
                    expires
            );
        }
        analysisContainer.addTable("banTable", banTable);

        return analysisContainer;
    }

    @Override
    public boolean isBanned(UUID uuid) {
        try {
            return !db.getBans(uuid).isEmpty();
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return false;
    }

    @Override
    public Collection<UUID> filterBanned(Collection<UUID> collection) {
        try {
            List<BanObject> bans = db.getBans();
            Set<UUID> banned = new HashSet<>();
            for (BanObject ban : bans) {
                banned.add(ban.getUuid());
            }

            return collection.stream().filter(banned::contains).collect(Collectors.toSet());
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return new HashSet<>();
    }
}