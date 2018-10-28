/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
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
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;
import com.djrapitops.plan.utilities.html.structure.TabsElement;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for LiteBans plugin.
 *
 * @author Rsl1122
 */
class LiteBansData extends PluginData implements BanData {

    private final LiteBansDatabaseQueries db;

    private final Formatter<Long> timestampFormatter;

    LiteBansData(
            LiteBansDatabaseQueries db,
            Formatter<Long> timestampFormatter
    ) {
        super(ContainerSize.TAB, "LiteBans");
        this.timestampFormatter = timestampFormatter;
        setPluginIcon(Icon.called("ban").of(Color.RED).build());
        this.db = db;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {

        inspectContainer.addValue(Icon.called("balance-scale").of(Color.RED) +
                " Hover over 'What' column entry for offence reasons", "");

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
                    String expires = expiry <= 0 ? "Never" : timestampFormatter.apply(expiry);
                    table.addRow(
                            "<span title=\"" + ban.getReason() + "\">Ban</span>",
                            Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(ban.getBannedBy()), ban.getBannedBy()),
                            expires
                    );
                }
                for (LiteBansDBObj mute : mutes) {
                    long expiry = mute.getExpiry();
                    String expires = expiry <= 0 ? "Never" : timestampFormatter.apply(expiry);
                    table.addRow(
                            "<span title=\"" + mute.getReason() + "\">Mute</span>",
                            Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(mute.getBannedBy()), mute.getBannedBy()),
                            expires
                    );
                }
                for (LiteBansDBObj warn : warns) {
                    long expiry = warn.getExpiry();
                    String expires = expiry <= 0 ? "Never" : timestampFormatter.apply(expiry);
                    table.addRow(
                            "<span title=\"" + warn.getReason() + "\">Warning</span>",
                            Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(warn.getBannedBy()), warn.getBannedBy()),
                            expires
                    );
                }
                for (LiteBansDBObj kick : kicks) {
                    long expiry = kick.getExpiry();
                    String expires = expiry <= 0 ? "Never" : timestampFormatter.apply(expiry);
                    table.addRow(
                            "<span title=\"" + kick.getReason() + "\">Kick</span>",
                            Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(kick.getBannedBy()), kick.getBannedBy()),
                            expires
                    );
                }
            }
        } catch (DBOpException ex) {
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
        TableContainer banTable = createTableContainer(banned, by);
        banTable.useJqueryDataTables();
        addRows(banTable, db.getBans());
        return banTable;
    }

    private TableContainer getMuteTable() {
        String muted = getWithIcon("Muted", Icon.called("bell-slash").of(Family.REGULAR));
        String by = getWithIcon("Muted By", Icon.called("gavel"));
        TableContainer muteTable = createTableContainer(muted, by);
        muteTable.useJqueryDataTables();
        addRows(muteTable, db.getMutes());
        return muteTable;
    }

    private TableContainer getWarningTable() {
        String warned = getWithIcon("Warned", Icon.called("exclamation-triangle"));
        String by = getWithIcon("Warned By", Icon.called("gavel"));
        TableContainer warnTable = createTableContainer(warned, by);
        warnTable.useJqueryDataTables();
        addRows(warnTable, db.getWarnings());
        return warnTable;
    }

    private TableContainer getKickTable() {
        String kicked = getWithIcon("Kicked", Icon.called("user-times"));
        String by = getWithIcon("Kicked By", Icon.called("gavel"));
        TableContainer kickTable = createTableContainer(kicked, by);
        kickTable.useJqueryDataTables();
        addRows(kickTable, db.getKicks());
        return kickTable;
    }

    private TableContainer createTableContainer(String who, String by) {
        String reason = getWithIcon("Reason", Icon.called("balance-scale"));
        String given = getWithIcon("Given", Icon.called("clock").of(Family.REGULAR));
        String expiry = getWithIcon("Expires", Icon.called("calendar-times").of(Family.REGULAR));
        String active = getWithIcon("Active", Icon.called("hourglass"));

        return new TableContainer(who, by, reason, given, expiry, active);
    }

    private void addRows(TableContainer table, List<LiteBansDBObj> objects) {
        if (objects.isEmpty()) {
            table.addRow("No Data");
        } else {
            Map<UUID, String> playerNames = Optional.ofNullable(analysisData)
                    .flatMap(c -> c.getValue(AnalysisKeys.PLAYER_NAMES)).orElse(new HashMap<>());
            for (LiteBansDBObj object : objects) {
                UUID uuid = object.getUuid();
                String name = playerNames.getOrDefault(uuid, uuid.toString());
                long expiry = object.getExpiry();
                String expires = expiry <= 0 ? "Never" : timestampFormatter.apply(expiry);
                long time = object.getTime();
                String given = time <= 0 ? "Unknown" : timestampFormatter.apply(time);

                table.addRow(
                        Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(name), name),
                        Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(object.getBannedBy()), object.getBannedBy()),
                        object.getReason(),
                        given,
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
            return false;
        }
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
            return new HashSet<>();
        }
    }
}