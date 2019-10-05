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
package com.djrapitops.plan.addons.placeholderapi.placeholders;

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.mutators.PingMutator;
import com.djrapitops.plan.delivery.domain.mutators.PvpInfoMutator;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.utilities.Predicates;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.UUID;

/**
 * Placeholders about a player.
 *
 * @author aidn5, Rsl1122
 */
public class PlayerPlaceHolder extends AbstractPlanPlaceHolder {

    private final DBSystem dbSystem;
    private Formatter<Long> year;

    public PlayerPlaceHolder(
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        super(serverInfo);
        this.dbSystem = dbSystem;
        year = formatters.yearLong();
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) throws Exception {
        Serializable got = get(params, p.getUniqueId());
        return got != null ? got.toString() : null;
    }

    // Checkstyle.OFF: CyclomaticComplexity

    public Serializable get(String params, UUID playerUUID) {
        PlayerContainer player = getPlayer(playerUUID);

        switch (params.toLowerCase()) {
            case "player_banned":
                return player.getValue(PlayerKeys.BANNED)
                        .orElse(Boolean.FALSE) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
            case "player_operator":
                return player.getValue(PlayerKeys.OPERATOR)
                        .orElse(Boolean.FALSE) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();

            case "player_sessions_count":
                return SessionsMutator.forContainer(player).count();

            case "player_kick_count":
                return player.getValue(PlayerKeys.KICK_COUNT).orElse(0);
            case "player_death_count":
                return player.getValue(PlayerKeys.DEATH_COUNT).orElse(0);
            case "player_mob_kill_count":
                return player.getValue(PlayerKeys.MOB_KILL_COUNT).orElse(0);
            case "player_player_kill_count":
                return player.getValue(PlayerKeys.PLAYER_KILL_COUNT).orElse(0);
            case "player_kill_death_ratio":
                return PvpInfoMutator.forContainer(player).killDeathRatio();

            case "player_ping_average_day":
                return PingMutator.forContainer(player).filterBy(Predicates.within(dayAgo(), now())).average();
            case "player_ping_average_week":
                return PingMutator.forContainer(player).filterBy(Predicates.within(weekAgo(), now())).average();

            case "player_ping_average_month":
                return PingMutator.forContainer(player).filterBy(Predicates.within(monthAgo(), now())).average();

            case "player_lastseen":
                return year.apply(player.getValue(PlayerKeys.LAST_SEEN).orElse((long) 0));
            case "player_registered":
                return year.apply(player.getValue(PlayerKeys.REGISTERED).orElse((long) 0));

            case "player_time_active":
                return year.apply(SessionsMutator.forContainer(player)
                        .toActivePlaytime());
            case "player_time_afk":
                return year.apply(SessionsMutator.forContainer(player)
                        .toAfkTime());

            case "player_time_total":
                return year.apply(SessionsMutator.forContainer(player)
                        .toPlaytime());
            case "player_time_day":
                return year.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(dayAgo(), now())
                        .toPlaytime());
            case "player_time_week":
                return year.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(weekAgo(), now())
                        .toPlaytime());
            case "player_time_month":
                return year.apply(SessionsMutator.forContainer(player)
                        .filterSessionsBetween(monthAgo(), now())
                        .toPlaytime());

        }
        return null;
    }

    // Checkstyle.ON: CyclomaticComplexity

    private PlayerContainer getPlayer(UUID playerUUID) {
        return dbSystem.getDatabase().query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
    }
}
