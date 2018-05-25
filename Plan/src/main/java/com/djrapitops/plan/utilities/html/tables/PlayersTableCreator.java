package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.calculation.ActivityIndex;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.List;
import java.util.UUID;

/**
 * @author Rsl1122
 */
// TODO Start using TableContainer for both tables
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

        long now = System.currentTimeMillis();
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
                    Log.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                    Log.toLog(PlayersTableCreator.class.getName(), e);
                }
            }

            i++;
        }

        return html.toString();
    }
}
