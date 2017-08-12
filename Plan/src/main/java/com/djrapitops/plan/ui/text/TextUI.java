package main.java.com.djrapitops.plan.ui.text;

import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.settings.DefaultMessages;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.analysis.*;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import org.bukkit.ChatColor;

import java.util.UUID;

/**
 * @author Rsl1122
 */
public class TextUI {

    /**
     * Constructor used to hide the public constructor
     */
    private TextUI() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param uuid
     * @return
     */
    public static String[] getInspectMessages(UUID uuid) {
        InspectCacheHandler inspectCache = Plan.getInstance().getInspectCache();
        long now = MiscUtils.getTime();
        if (!inspectCache.isCached(uuid)) {
            return new String[]{"Error has occurred, please retry."};
        }
        UserData d = inspectCache.getFromCache(uuid);

        ColorScheme cs = Plan.getInstance().getColorScheme();
        String main = cs.getMainColor();
        String sec = cs.getSecondaryColor();
        String ter = cs.getTertiaryColor();

        boolean active = AnalysisUtils.isActive(now, d.getLastPlayed(), d.getPlayTime(), d.getLoginTimes());
        boolean banned = d.isBanned();
        boolean online = d.isOnline();
        String ball = sec + " " + DefaultMessages.BALL + main;
        return new String[]{
                sec + " " + DefaultMessages.BALL + (banned ? ChatColor.DARK_RED + " Banned" : ter + (active ? " Active" : " Inactive")) + (online ? ChatColor.GREEN + " Online" : ChatColor.RED + " Offline"),
                ball + " Registered: " + sec + FormatUtils.formatTimeStampYear(d.getRegistered()),
                ball + " Last seen: " + sec + FormatUtils.formatTimeStamp(d.getLastPlayed()),
                ball + " Playtime: " + sec + FormatUtils.formatTimeAmount(d.getPlayTime()),
                ball + " Login times: " + sec + d.getLoginTimes(),
                ball + " Average session length: " + sec + FormatUtils.formatTimeAmount(MathUtils.averageLong(AnalysisUtils.transformSessionDataToLengths(d.getSessions()))),
                ball + " Kills: " + sec + d.getPlayerKills().size() + main + " Mobs: " + sec + d.getMobKills() + main + " Deaths: " + sec + d.getDeaths(),
                ball + " Geolocation: " + sec + d.getGeolocation()
        };
    }

    /**
     * @return
     */
    public static String[] getAnalysisMessages() {
        AnalysisCacheHandler analysisCache = Plan.getInstance().getAnalysisCache();
        if (!analysisCache.isCached()) {
            return new String[]{"Error has occurred, please retry."};
        }
        AnalysisData d = analysisCache.getData();

        ColorScheme cs = Plan.getInstance().getColorScheme();
        String main = cs.getMainColor();
        String sec = cs.getSecondaryColor();

        String ball = sec + " " + DefaultMessages.BALL + main;
        final ActivityPart activity = d.getActivityPart();
        final JoinInfoPart join = d.getJoinInfoPart();
        final KillPart kills = d.getKillPart();
        final PlaytimePart playtime = d.getPlaytimePart();
        final PlayerCountPart count = d.getPlayerCountPart();
        final TPSPart tps = d.getTpsPart();
        return new String[]{
                ball + " Total Players: " + sec + count.getPlayerCount(),

                ball + " Active: " + sec + activity.getActive().size()
                        + main + " Inactive: " + sec + activity.getInactive().size()
                        + main + " Single Join: " + sec + activity.getJoinedOnce().size()
                        + main + " Banned: " + sec + activity.getBans().size(),

                ball + " New Players 24h: " + sec + join.get("npday") + main + " 7d: " + sec + d.get("npweek") + main + " 30d: " + sec + d.get("npmonth"),
                "",
                ball + " Total Playtime: " + sec + playtime.get("totalplaytime") + main + " Player Avg: " + sec + playtime.get("avgplaytime"),
                ball + " Average Session Length: " + sec + activity.get("sessionaverage"),
                ball + " Total Logintimes: " + sec + join.getLoginTimes(),
                ball + " Kills: " + sec + kills.getAllPlayerKills().size() + main + " Mobs: " + sec + kills.getMobKills() + main + " Deaths: " + sec + kills.getDeaths(),
                "",
                ball + " Average TPS 24h: " + sec + tps.get("averagetpsday")
        };
    }

}
