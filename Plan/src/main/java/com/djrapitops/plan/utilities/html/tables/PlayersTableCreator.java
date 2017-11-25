package main.java.com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.PlayerProfile;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.comparators.PlayerProfileLastPlayedComparator;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.util.List;
import java.util.UUID;

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

        players.sort(new PlayerProfileLastPlayedComparator());

        StringBuilder html = new StringBuilder();

        long now = MiscUtils.getTime();
        UUID serverUUID = MiscUtils.getIPlan().getServerUuid();

        int i = 0;
        for (PlayerProfile profile : players) {
            if (i >= 2000) {
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
}
