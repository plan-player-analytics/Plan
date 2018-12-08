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
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Factory class for objects that represent HTML tables.
 *
 * @author Rsl1122
 */
@Singleton
public class HtmlTables {

    private final PlanConfig config;
    private final Formatters formatters;

    @Inject
    public HtmlTables(
            PlanConfig config,
            Formatters formatters
    ) {
        this.config = config;
        this.formatters = formatters;
    }

    /**
     * Create a new Command usage table.
     *
     * @param container Container that supports ServerKeys.COMMAND_USAGE.
     * @return a new {@link CommandUseTable}.
     */
    public TableContainer commandUseTable(DataContainer container) {
        return new CommandUseTable(container);
    }

    /**
     * Create a new Deaths table.
     *
     * @param deaths List of {@link PlayerDeath}s to be added to the table.
     * @return a new {@link DeathsTable}.
     */
    public TableContainer deathsTable(List<PlayerDeath> deaths) {
        return new DeathsTable(deaths, formatters.year());
    }

    /**
     * Create a new GeoInfo table.
     *
     * @param geoInfo List of {@link GeoInfo} to be added to the table.
     * @return a new {@link GeoInfoTable}.
     */
    public TableContainer geoInfoTable(List<GeoInfo> geoInfo) {
        return new GeoInfoTable(geoInfo, config.isTrue(DisplaySettings.PLAYER_IPS), formatters.year());
    }

    /**
     * Create a new Kill table.
     *
     * @param kills List of {@link PlayerKill]s to be added to the table.
     * @param color Color the table header should be.
     * @return a new {@link KillsTable}.
     */
    public TableContainer killsTable(List<PlayerKill> kills, String color) {
        return new KillsTable(kills, color, formatters.year());
    }

    /**
     * Create a new Nickname table.
     *
     * @param nicknames   List of {@link Nickname}s to be added to the table.
     * @param serverNames Names of the servers, for the server column. // TODO Move Server names to Nickname object.
     * @return a new {@link NicknameTable}.
     */
    public TableContainer nicknameTable(List<Nickname> nicknames, Map<UUID, String> serverNames) {
        return new NicknameTable(nicknames, serverNames, formatters.year());
    }

    /**
     * Create a new Country - Ping table.
     *
     * @param pingPerCountry Map of {@link Ping}s sorted by country names.
     * @return a new {@link PingTable}.
     */
    public TableContainer pingTable(Map<String, List<Ping>> pingPerCountry) {
        return new PingTable(pingPerCountry, formatters.decimals());
    }

    /**
     * Create a new Session table for a player.
     *
     * @param playerName Name of the player.
     * @param sessions   List of {@link Session}s the player has.
     * @return a new {@link PlayerSessionTable}.
     */
    public TableContainer playerSessionTable(String playerName, List<Session> sessions) {
        return new PlayerSessionTable(
                playerName, sessions,
                config.get(DisplaySettings.SESSIONS_PER_PAGE), config.getWorldAliasSettings(), formatters.year(), formatters.timeAmount()
        );
    }

    /**
     * Create a new Session table for a server.
     *
     * @param playerNames Map of UUID - Name pairs of the players. // TODO Move Player names to Session object.
     * @param sessions    List of {@link Session}s that occurred on the server.
     * @return a new {@link ServerSessionTable}.
     */
    public TableContainer serverSessionTable(Map<UUID, String> playerNames, List<Session> sessions) {
        return new ServerSessionTable(
                playerNames, sessions,
                config.get(DisplaySettings.SESSIONS_PER_PAGE), config.getWorldAliasSettings(), formatters.year(), formatters.timeAmount()
        );
    }

    /**
     * Create a Player table for a server.
     *
     * @param players List of {@link PlayerContainer}s of players who have played on the server.
     * @return a new {@link PlayersTable}.
     */
    public TableContainer playerTableForServerPage(List<PlayerContainer> players) {
        return new PlayersTable(
                players,
                config.get(DisplaySettings.PLAYERS_PER_SERVER_PAGE),
                config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD),
                config.get(TimeSettings.ACTIVE_LOGIN_THRESHOLD),
                formatters.timeAmount(), formatters.yearLong(), formatters.decimals()
        );
    }

    /**
     * Create a Player table for a players page.
     *
     * @param players List of {@link PlayerContainer}s of players.
     * @return a new {@link PlayersTable}.
     */
    public TableContainer playerTableForPlayersPage(List<PlayerContainer> players) {
        return new PlayersTable(
                players, config.get(DisplaySettings.PLAYERS_PER_PLAYERS_PAGE),
                config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD),
                config.get(TimeSettings.ACTIVE_LOGIN_THRESHOLD),
                formatters.timeAmount(), formatters.yearLong(), formatters.decimals()
        );
    }

    /**
     * Create a new Player table that contains Plugin Data.
     *
     * @param containers PluginData AnalysisContainers.
     * @param players    List of {@link PlayerContainer}s of players.
     * @return a new {@link PluginPlayersTable}.
     */
    public TableContainer pluginPlayersTable(Map<PluginData, AnalysisContainer> containers, Collection<PlayerContainer> players) {
        return new PluginPlayersTable(containers, players, config.get(DisplaySettings.PLAYERS_PER_SERVER_PAGE));
    }
}