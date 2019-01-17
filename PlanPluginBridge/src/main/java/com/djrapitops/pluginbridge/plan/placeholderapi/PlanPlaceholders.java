/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.pluginbridge.plan.placeholderapi;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.*;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Placeholders of Plan.
 *
 * @author Rsl1122
 */
@Singleton
public class PlanPlaceholders extends PlaceholderExpansion {

    private final PlanPlugin plugin;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final WebServer webServer;
    private final Formatters formatters;
    private final ErrorHandler errorHandler;

    public PlanPlaceholders(
            PlanPlugin plugin,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            WebServer webServer,
            Formatters formatters,
            ErrorHandler errorHandler
    ) {
        this.plugin = plugin;
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.webServer = webServer;
        this.formatters = formatters;
        this.errorHandler = errorHandler;
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
                return webServer.getAccessAddress();
            default:
                return null;
        }
    }

    private Serializable getServerValue(String identifier) {
        ServerContainer serverContainer = dbSystem.getDatabase().fetch().getServerContainer(serverInfo.getServerUUID());

        long now = System.currentTimeMillis();
        long dayAgo = now - TimeUnit.DAYS.toMillis(1L);
        long weekAgo = now - TimeAmount.WEEK.toMillis(1L);
        long monthAgo = now - TimeAmount.MONTH.toMillis(1L);

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
                    return formatters.timeAmount().apply(new SessionsMutator(playersMutator.getSessions()).toPlaytime());
                case "session_avg":
                    return formatters.timeAmount().apply(new SessionsMutator(playersMutator.getSessions()).toAverageSessionLength());
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
                    return TPSMutator.forContainer(serverContainer).filterDataBetween(weekAgo, now)
                            .lowTpsSpikeCount(config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED));
                default:
                    break;
            }
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
        return null;
    }

    private Serializable getPlayerValue(Player player, String identifier) {
        UUID uuid = player.getUniqueId();
        PlayerContainer playerContainer = dbSystem.getDatabase().fetch().getPlayerContainer(uuid);

        long now = System.currentTimeMillis();
        long dayAgo = now - TimeUnit.DAYS.toMillis(1L);
        long weekAgo = now - TimeAmount.WEEK.toMillis(1L);
        long monthAgo = now - TimeAmount.MONTH.toMillis(1L);

        try {
            SessionsMutator sessionsMutator = SessionsMutator.forContainer(playerContainer);
            switch (identifier.toLowerCase()) {
                case "playtime":
                    return formatters.timeAmount().apply(sessionsMutator.toPlaytime());
                case "playtime_day":
                    return formatters.timeAmount().apply(sessionsMutator.filterSessionsBetween(dayAgo, now).toPlaytime());
                case "playtime_week":
                    return formatters.timeAmount().apply(sessionsMutator.filterSessionsBetween(weekAgo, now).toPlaytime());
                case "playtime_month":
                    return formatters.timeAmount().apply(sessionsMutator.filterSessionsBetween(monthAgo, now).toPlaytime());
                case "geolocation":
                    return GeoInfoMutator.forContainer(playerContainer).mostRecent().map(GeoInfo::getGeolocation).orElse("Unknown");
                case "activity_index":
                    ActivityIndex activityIndex = playerContainer.getActivityIndex(
                            now,
                            config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD),
                            config.get(TimeSettings.ACTIVE_LOGIN_THRESHOLD)
                    );
                    return activityIndex.getValue() + " (" + activityIndex.getGroup() + ")";
                case "registered":
                    return formatters.yearLong().apply(playerContainer.getValue(PlayerKeys.REGISTERED).orElse(0L));
                case "last_seen":
                    return formatters.yearLong().apply(playerContainer.getValue(PlayerKeys.LAST_SEEN).orElse(0L));
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
            errorHandler.log(L.WARN, this.getClass(), e);
        }
        return null;
    }

}