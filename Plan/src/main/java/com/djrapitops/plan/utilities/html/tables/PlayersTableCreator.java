package main.java.com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.PlayerProfile;
import main.java.com.djrapitops.plan.data.additional.AnalysisContainer;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.data.additional.TableContainer;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.html.Html;
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
        UUID serverUUID = MiscUtils.getIPlan().getServerUuid();

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

                String activityString = isBanned ? "Banned" : FormatUtils.cutDecimals(profile.getActivityIndex(now));

                String geoLocation = profile.getMostRecentGeoInfo().getGeolocation();
                html.append(Html.TABLELINE_PLAYERS.parse(
                        Html.LINK_EXTERNAL.parse(Plan.getPlanAPI().getPlayerInspectPageLink(profile.getName()), profile.getName()),
                        activityString,
                        String.valueOf(playtime), FormatUtils.formatTimeAmount(playtime),
                        String.valueOf(loginTimes),
                        String.valueOf(registered), FormatUtils.formatTimeStampYear(registered),
                        String.valueOf(lastSeen), lastSeen != 0 ? FormatUtils.formatTimeStamp(lastSeen) : "-",
                        String.valueOf(geoLocation)
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
                String link = Html.LINK_EXTERNAL.parse(Plan.getPlanAPI().getPlayerInspectPageLink(profile.getName()), profile.getName());

                String[] playerData = FormatUtils.mergeArrays(new String[]{link}, sortedData.getOrDefault(uuid, new String[]{}));
                tableContainer.addRow(ArrayUtils.addAll(playerData));

                i++;
            }
        } catch (IllegalArgumentException ignored) {
        }
        return tableContainer.parseHtml().replace(Html.TABLE_SCROLL.parse(), "<table class=\"table table-bordered table-striped table-hover player-table dataTable\">");
    }


}
