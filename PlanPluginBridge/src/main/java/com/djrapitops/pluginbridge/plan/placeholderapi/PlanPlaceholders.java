package com.djrapitops.pluginbridge.plan.placeholderapi;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.*;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.UUID;

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
            default:
                return null;
        }
    }

    private Serializable getServerValue(String identifier) {
        ServerContainer serverContainer = Database.getActive().fetch().getServerContainer(ServerInfo.getServerUUID());

        long now = System.currentTimeMillis();
        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        try {
            PlayersMutator playersMutator = PlayersMutator.forContainer(serverContainer);
            switch (identifier.toLowerCase()) {
                case "players_total":
                    return playersMutator.count();
                case "players_new_day":
                    return playersMutator.filterRegisteredBetween(dayAgo, now).count();
                case "players_new_week":
                    return playersMutator.filterRegisteredBetween(weekAgo, now).count();
                case "players_new_month":
                    return playersMutator.filterRegisteredBetween(monthAgo, now).count();
                case "players_unique_day":
                    return playersMutator.filterPlayedBetween(dayAgo, now).count();
                case "players_unique_week":
                    return playersMutator.filterPlayedBetween(weekAgo, now).count();
                case "players_unique_month":
                    return playersMutator.filterPlayedBetween(monthAgo, now).count();
                case "playtime_total":
                    return Formatters.timeAmount().apply(new SessionsMutator(playersMutator.getSessions()).toPlaytime());
                case "session_avg":
                    return Formatters.timeAmount().apply(new SessionsMutator(playersMutator.getSessions()).toAverageSessionLength());
                case "session_count":
                    return playersMutator.getSessions().size();
                case "kills_players":
                    return new SessionsMutator(playersMutator.getSessions()).toPlayerKillCount();
                case "kills_mobs":
                    return new SessionsMutator(playersMutator.getSessions()).toMobKillCount();
                case "deaths_total":
                    return new SessionsMutator(playersMutator.getSessions()).toDeathCount();
                case "tps_day":
                    return TPSMutator.forContainer(serverContainer).filterDataBetween(dayAgo, now).averageTPS();
                case "tps_drops_week":
                    return TPSMutator.forContainer(serverContainer).filterDataBetween(weekAgo, now).lowTpsSpikeCount();
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
        PlayerContainer playerContainer = Database.getActive().fetch().getPlayerContainer(uuid);

        long now = System.currentTimeMillis();
        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        try {
            SessionsMutator sessionsMutator = SessionsMutator.forContainer(playerContainer);
            switch (identifier.toLowerCase()) {
                case "playtime":
                    return Formatters.timeAmount().apply(sessionsMutator.toPlaytime());
                case "playtime_day":
                    return Formatters.timeAmount().apply(sessionsMutator.filterSessionsBetween(dayAgo, now).toPlaytime());
                case "playtime_week":
                    return Formatters.timeAmount().apply(sessionsMutator.filterSessionsBetween(weekAgo, now).toPlaytime());
                case "playtime_month":
                    return Formatters.timeAmount().apply(sessionsMutator.filterSessionsBetween(monthAgo, now).toPlaytime());
                case "geolocation":
                    return GeoInfoMutator.forContainer(playerContainer).mostRecent().map(GeoInfo::getGeolocation).orElse("Unknown");
                case "activity_index":
                    ActivityIndex activityIndex = playerContainer.getActivityIndex(now);
                    return activityIndex.getValue() + " (" + activityIndex.getGroup() + ")";
                case "registered":
                    return Formatters.yearLongValue().apply(playerContainer.getValue(PlayerKeys.REGISTERED).orElse(0L));
                case "last_seen":
                    return Formatters.yearLongValue().apply(playerContainer.getValue(PlayerKeys.LAST_SEEN).orElse(0L));
                case "player_kills":
                    return sessionsMutator.toPlayerKillCount();
                case "mob_kills":
                    return sessionsMutator.toMobKillCount();
                case "deaths":
                    return sessionsMutator.toDeathCount();
                default:
                    break;
            }
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return null;
    }

}