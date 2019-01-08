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
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.utilities.ArrayUtil;

import java.io.Serializable;
import java.util.*;

/**
 * Html table that displays players data in various plugins.
 *
 * @author Rsl1122
 */
class PluginPlayersTable extends TableContainer {

    private Collection<PlayerContainer> players;

    private final int maxPlayers;
    private final boolean openPlayerPageInNewTab;

    PluginPlayersTable(
            Map<PluginData, AnalysisContainer> containers,
            Collection<PlayerContainer> players,
            int maxPlayers,
            boolean openPlayerPageInNewTab
    ) {
        this(getPluginDataSet(containers), players, maxPlayers, openPlayerPageInNewTab);
    }

    private PluginPlayersTable(
            TreeMap<String, Map<UUID, ? extends Serializable>> pluginDataSet,
            Collection<PlayerContainer> players,
            int maxPlayers,
            boolean openPlayerPageInNewTab
    ) {
        super(true, getHeaders(pluginDataSet.keySet()));

        this.players = players;
        this.maxPlayers = maxPlayers;
        this.openPlayerPageInNewTab = openPlayerPageInNewTab;

        useJqueryDataTables("player-plugin-table");

        if (players.isEmpty()) {
            addRow("No Players");
        } else {
            Map<UUID, Serializable[]> rows = getRows(pluginDataSet);
            addValues(rows);
        }
    }

    private static String[] getHeaders(Set<String> columnNames) {
        List<String> header = new ArrayList<>(columnNames);
        Collections.sort(header);
        return header.toArray(new String[0]);
    }

    private static TreeMap<String, Map<UUID, ? extends Serializable>> getPluginDataSet(Map<PluginData, AnalysisContainer> containers) {
        TreeMap<String, Map<UUID, ? extends Serializable>> data = new TreeMap<>();
        for (AnalysisContainer container : containers.values()) {
            if (!container.hasPlayerTableValues()) {
                continue;
            }
            data.putAll(container.getPlayerTableValues());
        }
        return data;
    }

    private void addValues(Map<UUID, Serializable[]> rows) {
        int i = 0;
        for (PlayerContainer profile : players) {
            if (i >= maxPlayers) {
                break;
            }

            UUID uuid = profile.getUnsafe(PlayerKeys.UUID);
            String name = profile.getValue(PlayerKeys.NAME).orElse("Unknown");
            Html link = openPlayerPageInNewTab ? Html.LINK_EXTERNAL : Html.LINK;
            String linkHtml = link.parse(PlanAPI.getInstance().getPlayerInspectPageLink(name), name);

            Serializable[] playerData = ArrayUtil.merge(new Serializable[]{linkHtml}, rows.getOrDefault(uuid, new Serializable[]{}));
            addRow(playerData);

            i++;
        }
    }

    private Map<UUID, Serializable[]> getRows(TreeMap<String, Map<UUID, ? extends Serializable>> data) {
        Map<UUID, Serializable[]> rows = new HashMap<>();

        int size = header.length - 1;
        for (PlayerContainer profile : players) {
            UUID uuid = profile.getUnsafe(PlayerKeys.UUID);

            Serializable[] row = new Serializable[size];
            for (int i = 0; i < size; i++) {
                String label = header[i + 1];

                Map<UUID, ? extends Serializable> playerSpecificData = data.getOrDefault(label, new HashMap<>());
                Serializable value = playerSpecificData.get(uuid);
                if (value != null) {
                    row[i] = value;
                }
            }
            rows.put(uuid, row);
        }
        return rows;
    }

}