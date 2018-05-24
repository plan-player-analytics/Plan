package com.djrapitops.pluginbridge.plan.placeholderapi;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.calculation.ActivityIndex;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.analysis.Analysis;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Placeholders of Plan.
 *
 * @author Rsl1122
 */
public class PlanPlaceholders extends PlaceholderExpansion {

    private final PlanPlugin plugin;

    public PlanPlaceholders() {
        plugin = PlanPlugin.getInstance();
    }

    @Override
    public String getIdentifier() {
        return "plan";
    }

    @Override
    public String getPlugin() {
        return "Plan";
    }

    @Override
    public String getAuthor() {
        return "Rsl1122";
    }

    @Override
    public String getVersion() {
        return plugin.getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        Serializable planValue = getPlanValue(identifier);
        if (planValue != null) {
            return planValue.toString();
        }
        Serializable serverValue = getServerValue(identifier);
        if (serverValue != null) {
            return serverValue.toString();
        }

        if (player != null) {
            Serializable playerValue = getPlayerValue(player, identifier);
            if (playerValue != null) {
                return playerValue.toString();
            }
        }

        return null;
    }

    private Serializable getPlanValue(String identifier) {
        switch (identifier.toLowerCase()) {
            case "address":
                return PlanSystem.getInstance().getWebServerSystem().getWebServer().getAccessAddress();
            case "analysis_refresh":
                Optional<Long> refreshDate = Analysis.getRefreshDate();
                if (refreshDate.isPresent()) {
                    return FormatUtils.formatTimeStampClock(refreshDate.get());
                }
                return "Not yet run";
            default:
                return null;
        }
    }

    private Serializable getServerValue(String identifier) {
        Callable<ServerProfile> serverProfile = Analysis::getServerProfile;

        long now = MiscUtils.getTime();
        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        try {
            switch (identifier.toLowerCase()) {
                case "players_total":
                    return serverProfile.call().getPlayerCount();
                case "players_new_day":
                    return serverProfile.call().getPlayersWhoRegistered(0, dayAgo).count();
                case "players_new_week":
                    return serverProfile.call().getPlayersWhoRegistered(0, weekAgo).count();
                case "players_new_month":
                    return serverProfile.call().getPlayersWhoRegistered(0, monthAgo).count();
                case "players_unique_day":
                    return serverProfile.call().getPlayersWhoPlayedBetween(0, dayAgo).count();
                case "players_unique_week":
                    return serverProfile.call().getPlayersWhoPlayedBetween(0, weekAgo).count();
                case "players_unique_month":
                    return serverProfile.call().getPlayersWhoPlayedBetween(0, monthAgo).count();
                case "playtime_total":
                    return FormatUtils.formatTimeAmount(serverProfile.call().getTotalPlaytime());
                case "session_avg":
                    return FormatUtils.formatTimeAmount(
                            PlayerProfile.getSessionAverage(serverProfile.call().getAllSessions().stream())
                    );
                case "session_count":
                    return serverProfile.call().getAllSessions().size();
                case "kills_players":
                    return PlayerProfile.getPlayerKills(serverProfile.call().getAllSessions().stream()).count();
                case "kills_mobs":
                    return PlayerProfile.getMobKillCount(serverProfile.call().getAllSessions().stream());
                case "deaths_total":
                    return PlayerProfile.getDeathCount(serverProfile.call().getAllSessions().stream());
                case "tps_day":
                    return FormatUtils.cutDecimals(
                            MathUtils.averageDouble(serverProfile.call().getTPSData(0, dayAgo).map(TPS::getTicksPerSecond))
                    );
                case "tps_drops_week":
                    return ServerProfile.getLowSpikeCount(serverProfile.call().getTPSData(0, weekAgo).collect(Collectors.toList()));
                default:
                    break;
            }
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return null;
    }

    private Serializable getPlayerValue(Player player, String identifier) {
        UUID uuid = player.getUniqueId();
        Callable<PlayerProfile> profile = () -> Database.getActive().fetch().getPlayerProfile(uuid);

        long now = MiscUtils.getTime();
        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        try {
            switch (identifier.toLowerCase()) {
                case "playtime":
                    return FormatUtils.formatTimeAmount(profile.call().getPlaytime(0, now));
                case "playtime_day":
                    return FormatUtils.formatTimeAmount(profile.call().getPlaytime(dayAgo, now));
                case "playtime_week":
                    return FormatUtils.formatTimeAmount(profile.call().getPlaytime(weekAgo, now));
                case "playtime_month":
                    return FormatUtils.formatTimeAmount(profile.call().getPlaytime(monthAgo, now));
                case "geolocation":
                    return profile.call().getMostRecentGeoInfo().getGeolocation();
                case "activity_index":
                    ActivityIndex activityIndex = profile.call().getActivityIndex(now);
                    return activityIndex.getValue() + " (" + activityIndex.getGroup() + ")";
                case "registered":
                    return FormatUtils.formatTimeAmount(profile.call().getRegistered());
                case "last_seen":
                    return FormatUtils.formatTimeAmount(profile.call().getLastSeen());
                case "player_kills":
                    return profile.call().getPlayerKills().count();
                case "mob_kills":
                    return profile.call().getMobKillCount();
                case "deaths":
                    return profile.call().getDeathCount();
                default:
                    break;
            }
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return null;
    }

}