package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.utilities.ArrayUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.*;

/**
 * TableContainer that creates the html table for per player plugins values.
 *
 * @author Rsl1122
 */
public class PluginPlayersTable extends TableContainer {

    private Collection<PlayerContainer> players;

    public PluginPlayersTable(Map<PluginData, AnalysisContainer> containers, Collection<PlayerContainer> players) {
        this(getPluginDataSet(containers), players);
    }

    private PluginPlayersTable(TreeMap<String, Map<UUID, ? extends Serializable>> pluginDataSet, Collection<PlayerContainer> players) {
        super(true, getHeaders(pluginDataSet.keySet()));

        this.players = players;

        useJqueryDataTables("player-plugin-table");

        if (players.isEmpty()) {
            addRow("No Players");
        } else {
            Map<UUID, String[]> rows = getRows(pluginDataSet);
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

    private void addValues(Map<UUID, String[]> rows) {
        int i = 0;
        int maxPlayers = Settings.MAX_PLAYERS.getNumber();
        if (maxPlayers <= 0) {
            maxPlayers = 2000;
        }
        for (PlayerContainer profile : players) {
            if (i >= maxPlayers) {
                break;
            }
            UUID uuid = profile.getUnsafe(PlayerKeys.UUID);
            String name = profile.getValue(PlayerKeys.NAME).orElse("Unknown");
            String link = Html.LINK_EXTERNAL.parse(PlanAPI.getInstance().getPlayerInspectPageLink(name), name);

            String[] playerData = ArrayUtil.merge(new String[]{link}, rows.getOrDefault(uuid, new String[]{}));
            addRow(ArrayUtils.addAll(playerData));

            i++;
        }
    }

    private Map<UUID, String[]> getRows(TreeMap<String, Map<UUID, ? extends Serializable>> data) {
        Map<UUID, String[]> rows = new HashMap<>();

        int size = header.length - 1;
        for (PlayerContainer profile : players) {
            UUID uuid = profile.getUnsafe(PlayerKeys.UUID);

            String[] row = new String[size];
            for (int i = 0; i < size; i++) {
                String label = header[i + 1];

                Map<UUID, ? extends Serializable> playerSpecificData = data.getOrDefault(label, new HashMap<>());
                Serializable value = playerSpecificData.get(uuid);
                if (value != null) {
                    row[i] = value.toString();
                } else {
                    row[i] = "-";
                }
            }
            rows.put(uuid, row);
        }
        return rows;
    }

}