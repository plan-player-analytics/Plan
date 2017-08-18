package main.java.com.djrapitops.plan.ui.html.tables;

import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;

import java.util.List;

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

    /**
     * @param data The list of the {@link UserData} Objects from which the players table should be created
     * @return The created players table
     */
    public static String createSortablePlayersTable(List<UserData> data) {
        StringBuilder html = new StringBuilder();

        long now = MiscUtils.getTime();
        boolean showImages = Settings.PLAYERLIST_SHOW_IMAGES.isTrue();

        int i = 0;
        for (UserData uData : data) {
            if (i >= 750) {
                break;
            }

            try {
                boolean isBanned = uData.isBanned();
                boolean isUnknown = uData.getLoginTimes() == 1;
                boolean isActive = AnalysisUtils.isActive(now, uData.getLastPlayed(), uData.getPlayTime(), uData.getLoginTimes());

                String activityString = getActivityString(isBanned, isUnknown, isActive);

                String img = showImages ? Html.MINOTAR_SMALL_IMG.parse(uData.getName()) : "";

                html.append(Html.TABLELINE_PLAYERS.parse(
                        img + Html.LINK.parse(HtmlUtils.getInspectUrl(uData.getName()), uData.getName()),
                        activityString,
                        String.valueOf(uData.getPlayTime()), FormatUtils.formatTimeAmount(uData.getPlayTime()),
                        String.valueOf(uData.getLoginTimes()),
                        String.valueOf(uData.getRegistered()), FormatUtils.formatTimeStampYear(uData.getRegistered()),
                        String.valueOf(uData.getLastPlayed()), FormatUtils.formatTimeStamp(uData.getLastPlayed()),
                        String.valueOf(uData.getGeolocation())
                ));
            } catch (NullPointerException ignored) {
            }

            i++;
        }

        return html.toString();
    }

    private static String getActivityString(boolean isBanned, boolean isUnknown, boolean isActive) {
        if (isBanned) {
            return "Banned";
        }

        if (isUnknown) {
            return "Unknown";
        }

        return isActive ? "Active" : "Inactive";
    }
}
