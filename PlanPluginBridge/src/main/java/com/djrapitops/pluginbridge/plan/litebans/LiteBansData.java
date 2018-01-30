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
import com.djrapitops.plan.utilities.html.structure.TabsElement;
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
        super(ContainerSize.TAB, "LiteBans");
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
            List<LiteBansDBObj> bans = db.getBans(uuid);
            if (bans.isEmpty()) {
                banTable.addRow("Not LiteBanned");
            } else {
                for (LiteBansDBObj ban : bans) {
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
        TableContainer banTable = getBanTable();
        TableContainer muteTable = getMuteTable();
        TableContainer warningTable = getWarningTable();
        TableContainer kickTable = getKickTable();

        Html spacing = Html.PANEL_BODY;
        String[] navAndHtml = new TabsElement(
                new TabsElement.Tab(getWithIcon("Bans", "ban"), spacing.parse(banTable.parseHtml())),
                new TabsElement.Tab(getWithIcon("Mutes", "bell-slash-o"), spacing.parse(muteTable.parseHtml())),
                new TabsElement.Tab(getWithIcon("Warnings", "exclamation-triangle"), spacing.parse(warningTable.parseHtml())),
                new TabsElement.Tab(getWithIcon("Kicks", "user-times"), spacing.parse(kickTable.parseHtml()))
        ).toHtml();
        analysisContainer.addHtml("Tables", navAndHtml[0] + navAndHtml[1]);

        return analysisContainer;
    }

    private TableContainer getBanTable() throws SQLException {
        String banned = getWithIcon("Banned", "ban");
        String by = getWithIcon("Banned By", "gavel");
        String reason = getWithIcon("Reason", "balance-scale");
        String date = getWithIcon("Expires", "calendar-times-o");
        String active = getWithIcon("Active", "hourglass");

        TableContainer banTable = new TableContainer(banned, by, reason, date, active);
        banTable.useJqueryDataTables();
        addRows(banTable, db.getBans());
        return banTable;
    }

    private TableContainer getMuteTable() throws SQLException {
        String muted = getWithIcon("Muted", "bell-slash-o");
        String by = getWithIcon("Muted By", "gavel");
        String reason = getWithIcon("Reason", "balance-scale");
        String date = getWithIcon("Expires", "calendar-times-o");
        String active = getWithIcon("Active", "hourglass");

        TableContainer muteTable = new TableContainer(muted, by, reason, date, active);
        muteTable.useJqueryDataTables();
        addRows(muteTable, db.getMutes());
        return muteTable;
    }

    private TableContainer getWarningTable() throws SQLException {
        String warned = getWithIcon("Warned", "exclamation-triangle");
        String by = getWithIcon("Warned By", "gavel");
        String reason = getWithIcon("Reason", "balance-scale");
        String date = getWithIcon("Expires", "calendar-times-o");
        String active = getWithIcon("Active", "hourglass");

        TableContainer warnTable = new TableContainer(warned, by, reason, date, active);
        warnTable.useJqueryDataTables();
        addRows(warnTable, db.getWarnings());
        return warnTable;
    }

    private TableContainer getKickTable() throws SQLException {
        String kicked = getWithIcon("Kicked", "user-times");
        String by = getWithIcon("Kicked By", "gavel");
        String reason = getWithIcon("Reason", "balance-scale");
        String date = getWithIcon("Expires", "calendar-times-o");
        String active = getWithIcon("Active", "hourglass");

        TableContainer kickTable = new TableContainer(kicked, by, reason, date, active);
        kickTable.useJqueryDataTables();
        addRows(kickTable, db.getKicks());
        return kickTable;
    }

    private void addRows(TableContainer table, List<LiteBansDBObj> objects) {
        if (objects.isEmpty()) {
            table.addRow("No Data");
        } else {
            for (LiteBansDBObj object : objects) {
                UUID uuid = object.getUuid();
                String name = DataCache.getInstance().getName(uuid);
                long expiry = object.getExpiry();
                String expires = expiry <= 0 ? "Never" : FormatUtils.formatTimeStampSecond(expiry);

                table.addRow(
                        Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(name), name),
                        Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(object.getBannedBy()), object.getBannedBy()),
                        object.getReason(),
                        expires,
                        object.isActive() ? "Yes" : "No"
                );
            }
        }
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
            List<LiteBansDBObj> bans = db.getBans();
            Set<UUID> banned = new HashSet<>();
            for (LiteBansDBObj ban : bans) {
                banned.add(ban.getUuid());
            }

            return collection.stream().filter(banned::contains).collect(Collectors.toSet());
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return new HashSet<>();
    }
}