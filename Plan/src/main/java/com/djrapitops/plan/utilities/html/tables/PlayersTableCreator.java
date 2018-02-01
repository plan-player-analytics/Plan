package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.calculation.ActivityIndex;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.api.utility.log.Log;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author Rsl1122
 */
public class PlayersTableCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private PlayersTableCreator() {
        throw new IllegalStateException("Utility class");
    }

    public static String createTable(List<PlayerProfile> players) {
        if (players.isEmpty()) {
            return Html.TABLELINE_PLAYERS.parse("<b>No Players</b>", "", "", "", "", "", "", "", "", "");
        }

        StringBuilder html = new StringBuilder();

        long now = MiscUtils.getTime();
        UUID serverUUID = ServerInfo.getServerUUID();

        int i = 0;
        int maxPlayers = Settings.MAX_PLAYERS.getNumber();
        if (maxPlayers <= 0) {
            maxPlayers = 2000;
        }
        for (PlayerProfile profile : players) {
            if (i >= maxPlayers) {
                break;
            }

            try {
                boolean isBanned = profile.isBanned();
                long loginTimes = profile.getSessionCount(serverUUID);
                long playtime = profile.getPlaytime(serverUUID);
                long registered = profile.getRegistered();

                long lastSeen = profile.getLastSeen();

                ActivityIndex activityIndex = profile.getActivityIndex(now);
                String activityGroup = activityIndex.getGroup();
                String activityString = activityIndex.getFormattedValue()
                        + (isBanned ? " (<b>Banned</b>)" : " (" + activityGroup + ")");

                String geoLocation = profile.getMostRecentGeoInfo().getGeolocation();
                html.append(Html.TABLELINE_PLAYERS.parse(
                        Html.LINK_EXTERNAL.parse(PlanAPI.getInstance().getPlayerInspectPageLink(profile.getName()), profile.getName()),
                        activityString,
                        playtime, FormatUtils.formatTimeAmount(playtime),
                        loginTimes,
                        registered, FormatUtils.formatTimeStampYear(registered),
                        lastSeen, lastSeen != 0 ? FormatUtils.formatTimeStampYear(lastSeen) : "-",
                        geoLocation
                ));
            } catch (NullPointerException e) {
                if (Settings.DEV_MODE.isTrue()) {
                    Log.toLog(PlayersTableCreator.class.getName(), e);
                }
            }

            i++;
        }

        return html.toString();
    }

    public static String createPluginsTable(Map<PluginData, AnalysisContainer> containers, List<PlayerProfile> players) {
        TreeMap<String, Map<UUID, ? extends Serializable>> data = new TreeMap<>();
        for (AnalysisContainer container : containers.values()) {
            if (!container.hasPlayerTableValues()) {
                continue;
            }
            data.putAll(container.getPlayerTableValues());
        }

        List<String> header = new ArrayList<>(data.keySet());
        Collections.sort(header);

        int size = header.size();
        TableContainer tableContainer = new TableContainer(true, header.toArray(new String[size]));

        try {
            if (players.isEmpty()) {
                tableContainer.addRow("<b>No Players</b>");
                throw new IllegalArgumentException("No players");
            }

            Map<UUID, String[]> sortedData = new HashMap<>();

            for (PlayerProfile profile : players) {
                UUID uuid = profile.getUuid();
                String[] playerdata = new String[size];
                for (int i = 0; i < size; i++) {
                    String label = header.get(i);
                    Map<UUID, ? extends Serializable> playerSpecificData = data.getOrDefault(label, new HashMap<>());
                    Serializable value = playerSpecificData.get(uuid);
                    if (value != null) {
                        playerdata[i] = value.toString();
                    } else {
                        playerdata[i] = "-";
                    }
                }
                sortedData.put(uuid, playerdata);
            }

            int i = 0;
            int maxPlayers = Settings.MAX_PLAYERS.getNumber();
            if (maxPlayers <= 0) {
                maxPlayers = 2000;
            }
            for (PlayerProfile profile : players) {
                if (i >= maxPlayers) {
                    break;
                }
                UUID uuid = profile.getUuid();
                String link = Html.LINK_EXTERNAL.parse(PlanAPI.getInstance().getPlayerInspectPageLink(profile.getName()), profile.getName());

                String[] playerData = FormatUtils.mergeArrays(new String[]{link}, sortedData.getOrDefault(uuid, new String[]{}));
                tableContainer.addRow(ArrayUtils.addAll(playerData));

                i++;
            }
        } catch (IllegalArgumentException ignored) {
        }
        return tableContainer.parseHtml().replace(Html.TABLE_SCROLL.parse(), "<table class=\"table table-bordered table-striped table-hover player-table dataTable\">");
    }


}
