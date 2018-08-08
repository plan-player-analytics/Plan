/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.BanData;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;
import com.djrapitops.plan.utilities.html.structure.TabsElement;
import com.djrapitops.plugin.api.utility.log.Log;

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
        setPluginIcon(Icon.called("ban").of(Color.RED).build());
        this.db = db;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {

        inspectContainer.addValue(Icon.called("balance-scale").of(Color.RED) +
                "Hover over 'What' column entry for offence reasons", "");

        String what = getWithIcon("Effect", Icon.called("times-circle").of(Family.REGULAR));
        String by = getWithIcon("By", Icon.called("gavel"));
        String date = getWithIcon("Expires", Icon.called("calendar-times").of(Family.REGULAR));
        TableContainer table = new TableContainer(what, by, date);
        table.setColor("red");

        try {
            List<LiteBansDBObj> bans = db.getBans(uuid);
            List<LiteBansDBObj> mutes = db.getMutes(uuid);
            List<LiteBansDBObj> warns = db.getWarnings(uuid);
            List<LiteBansDBObj> kicks = db.getKicks(uuid);
            if (bans.isEmpty() && mutes.isEmpty() && warns.isEmpty() && kicks.isEmpty()) {
                table.addRow("None");
            } else {
                for (LiteBansDBObj ban : bans) {
                    long expiry = ban.getExpiry();
                    String expires = expiry <= 0 ? "Never" : FormatUtils.formatTimeStampSecond(expiry);
                    table.addRow(
                            "<span title=\"" + ban.getReason() + "\">Ban</span>",
                            Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(ban.getBannedBy()), ban.getBannedBy()),
                            expires
                    );
                }
                for (LiteBansDBObj mute : mutes) {
                    long expiry = mute.getExpiry();
                    String expires = expiry <= 0 ? "Never" : FormatUtils.formatTimeStampSecond(expiry);
                    table.addRow(
                            "<span title=\"" + mute.getReason() + "\">Mute</span>",
                            Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(mute.getBannedBy()), mute.getBannedBy()),
                            expires
                    );
                }
                for (LiteBansDBObj warn : warns) {
                    long expiry = warn.getExpiry();
                    String expires = expiry <= 0 ? "Never" : FormatUtils.formatTimeStampSecond(expiry);
                    table.addRow(
                            "<span title=\"" + warn.getReason() + "\">Warning</span>",
                            Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(warn.getBannedBy()), warn.getBannedBy()),
                            expires
                    );
                }
                for (LiteBansDBObj kick : kicks) {
                    long expiry = kick.getExpiry();
                    String expires = expiry <= 0 ? "Never" : FormatUtils.formatTimeStampSecond(expiry);
                    table.addRow(
                            "<span title=\"" + kick.getReason() + "\">Kick</span>",
                            Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(kick.getBannedBy()), kick.getBannedBy()),
                            expires
                    );
                }
            }
        } catch (DBOpException ex) {
            Log.toLog(this.getClass().getName(), ex);
            table.addRow("Error: " + ex);
        } catch (IllegalStateException e) {
            inspectContainer.addValue(getWithIcon("Error", Icons.RED_WARN), "Database connection is not available");
            return inspectContainer;
        }
        inspectContainer.addTable("table", table);
        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        try {
            TableContainer banTable = getBanTable();
            TableContainer muteTable = getMuteTable();
            TableContainer warningTable = getWarningTable();
            TableContainer kickTable = getKickTable();

            Html spacing = Html.PANEL_BODY;
            String[] navAndHtml = new TabsElement(
                    new TabsElement.Tab(getWithIcon("Bans", Icon.called("ban")), spacing.parse(banTable.parseHtml())),
                    new TabsElement.Tab(getWithIcon("Mutes", Icon.called("bell-slash").of(Family.REGULAR)), spacing.parse(muteTable.parseHtml())),
                    new TabsElement.Tab(getWithIcon("Warnings", Icon.called("exclamation-triangle")), spacing.parse(warningTable.parseHtml())),
                    new TabsElement.Tab(getWithIcon("Kicks", Icon.called("user-times")), spacing.parse(kickTable.parseHtml()))
            ).toHtml();
            analysisContainer.addHtml("Tables", navAndHtml[0] + navAndHtml[1]);
        } catch (IllegalStateException e) {
            analysisContainer.addValue(getWithIcon("Error", Icons.RED_WARN), "Database connection is not available");
        }
        return analysisContainer;
    }

    private TableContainer getBanTable() {
        String banned = getWithIcon("Banned", Icon.called("ban"));
        String by = getWithIcon("Banned By", Icon.called("gavel"));
        String reason = getWithIcon("Reason", Icon.called("balance-scale"));
        String date = getWithIcon("Expires", Icon.called("calendar-times").of(Family.REGULAR));
        String active = getWithIcon("Active", Icon.called("hourglass"));

        TableContainer banTable = new TableContainer(banned, by, reason, date, active);
        banTable.useJqueryDataTables();
        addRows(banTable, db.getBans());
        return banTable;
    }

    private TableContainer getMuteTable() {
        String muted = getWithIcon("Muted", Icon.called("bell-slash").of(Family.REGULAR));
        String by = getWithIcon("Muted By", Icon.called("gavel"));
        String reason = getWithIcon("Reason", Icon.called("balance-scale"));
        String date = getWithIcon("Expires", Icon.called("calendar-times").of(Family.REGULAR));
        String active = getWithIcon("Active", Icon.called("hourglass"));

        TableContainer muteTable = new TableContainer(muted, by, reason, date, active);
        muteTable.useJqueryDataTables();
        addRows(muteTable, db.getMutes());
        return muteTable;
    }

    private TableContainer getWarningTable() {
        String warned = getWithIcon("Warned", Icon.called("exclamation-triangle"));
        String by = getWithIcon("Warned By", Icon.called("gavel"));
        String reason = getWithIcon("Reason", Icon.called("balance-scale"));
        String date = getWithIcon("Expires", Icon.called("calendar-times").of(Family.REGULAR));
        String active = getWithIcon("Active", Icon.called("hourglass"));

        TableContainer warnTable = new TableContainer(warned, by, reason, date, active);
        warnTable.useJqueryDataTables();
        addRows(warnTable, db.getWarnings());
        return warnTable;
    }

    private TableContainer getKickTable() {
        String kicked = getWithIcon("Kicked", Icon.called("user-times"));
        String by = getWithIcon("Kicked By", Icon.called("gavel"));
        String reason = getWithIcon("Reason", Icon.called("balance-scale"));
        String date = getWithIcon("Expires", Icon.called("calendar-times").of(Family.REGULAR));
        String active = getWithIcon("Active", Icon.called("hourglass"));

        TableContainer kickTable = new TableContainer(kicked, by, reason, date, active);
        kickTable.useJqueryDataTables();
        addRows(kickTable, db.getKicks());
        return kickTable;
    }

    private void addRows(TableContainer table, List<LiteBansDBObj> objects) {
        if (objects.isEmpty()) {
            table.addRow("No Data");
        } else {
            Map<UUID, String> playerNames = analysisData.getValue(AnalysisKeys.PLAYER_NAMES).orElse(new HashMap<>());
            for (LiteBansDBObj object : objects) {
                UUID uuid = object.getUuid();
                String name = playerNames.getOrDefault(uuid, uuid.toString());
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
            return db.getBans(uuid).stream().anyMatch(LiteBansDBObj::isActive);
        } catch (DBOpException e) {
            Log.toLog(this.getClass(), e);
        }
        return false;
    }

    @Override
    public Collection<UUID> filterBanned(Collection<UUID> collection) {
        try {
            Set<UUID> banned = db.getBans().stream()
                    .filter(LiteBansDBObj::isActive)
                    .map(LiteBansDBObj::getUuid)
                    .collect(Collectors.toSet());

            return collection.stream().filter(banned::contains).collect(Collectors.toSet());
        } catch (DBOpException e) {
            Log.toLog(this.getClass(), e);
        }
        return new HashSet<>();
    }
}