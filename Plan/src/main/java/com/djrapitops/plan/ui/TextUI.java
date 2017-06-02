package main.java.com.djrapitops.plan.ui;

import java.util.UUID;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.ChatColor;

/**
 *
 * @author Rsl1122
 */
public class TextUI {

    /**
     *
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
        ChatColor main = Phrase.COLOR_MAIN.color();
        ChatColor sec = Phrase.COLOR_SEC.color();
        ChatColor ter = Phrase.COLOR_TER.color();
        boolean active = AnalysisUtils.isActive(now, d.getLastPlayed(), d.getPlayTime(), d.getLoginTimes());
        boolean banned = d.isBanned();
        boolean online = d.isOnline();
        String ball = sec + " " + Phrase.BALL + main;
        return new String[]{
            sec + " " + Phrase.BALL + (banned ? ChatColor.DARK_RED + " Banned" : ter + (active ? " Active" : " Inactive")) + (online ? ChatColor.GREEN + " Online" : ChatColor.RED + " Offline"),
            ball + " Registered: " + sec + FormatUtils.formatTimeStamp(d.getRegistered()),
            ball + " Last seen: " + sec + FormatUtils.formatTimeStamp(d.getLastPlayed()),
            ball + " Playtime: " + sec + FormatUtils.formatTimeAmount(d.getPlayTime()),
            ball + " Login times: " + sec + d.getLoginTimes(),
            ball + " Average session length: " + sec + FormatUtils.formatTimeAmount(MathUtils.averageLong(AnalysisUtils.transformSessionDataToLengths(d.getSessions()))),
            ball + " Kills: " + sec + d.getPlayerKills().size() + main + " Mobs: " + sec + d.getMobKills() + main + " Deaths: " + sec + d.getDeaths(),
            ball + " Geolocation: " + sec + d.getDemData().getGeoLocation()
        };
    }

    /**
     *
     * @return
     */
    public static String[] getAnalysisMessages() {
        AnalysisCacheHandler analysisCache = Plan.getInstance().getAnalysisCache();
        if (!analysisCache.isCached()) {
            return new String[]{"Error has occurred, please retry."};
        }
        AnalysisData d = analysisCache.getData();
        ChatColor main = Phrase.COLOR_MAIN.color();
        ChatColor sec = Phrase.COLOR_SEC.color();
        ChatColor ter = Phrase.COLOR_TER.color();
        String ball = sec + " " + Phrase.BALL + main;
        return new String[]{
            ball + " Total Players: " + sec + d.getTotal(),
            ball + " Active: " + sec + d.getActive() + main + " Inactive: " + sec + d.getInactive() + main + " Single join: " + sec + d.getJoinleaver() + main + " Banned: " + sec + d.getBanned(),
            ball + " New Players 24h: " + sec + d.getNewPlayersDay() + main + " 7d: " + sec + d.getNewPlayersWeek() + main + " 30d: " + sec + d.getNewPlayersMonth(),
            "",
            ball + " Total Playtime: " + sec + FormatUtils.formatTimeAmount(d.getTotalPlayTime()) + main + " Player Avg: " + sec + FormatUtils.formatTimeAmount(d.getAveragePlayTime()),
            ball + " Average Session Length: " + sec + FormatUtils.formatTimeAmount(d.getSessionAverage()),
            ball + " Total Logintimes: " + sec + d.getTotalLoginTimes(),
            ball + " Kills: " + sec + d.getTotalPlayerKills() + main + " Mobs: " + sec + d.getTotalMobKills() + main + " Deaths: " + sec + d.getTotalDeaths()
        };
    }

}
