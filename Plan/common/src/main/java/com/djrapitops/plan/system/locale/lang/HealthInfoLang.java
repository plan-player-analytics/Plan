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
package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} enum for {@link com.djrapitops.plan.data.store.mutators.health.AbstractHealthInfo} related language.
 *
 * @author Rsl1122
 */
public enum HealthInfoLang implements Lang {
    REGULAR_ACTIVITY_REMAIN("Regular Activity Remain", " ${0} of regular players have remained active (${1}/${2})"),
    REGULAR_CHANGE("Regular Activity Change", " Number of regular players has "),
    REGULAR_CHANGE_INCREASE("Regular Activity Change Increase", "increased (+${0})"),
    REGULAR_CHANGE_ZERO("Regular Activity Change Zero", "stayed the same (+${0})"),
    REGULAR_CHANGE_DECREASE("Regular Activity Change Decrease", "decreased (${0})"),
    ACTIVE_PLAY_COMPARISON_INCREASE("Active Playtime Comparison Increase", " Active players seem to have things to do (Played ${0} vs ${1}, last two weeks vs weeks 2-4)"),
    ACTIVE_PLAY_COMPARISON_DECREASE("Active Playtime Comparison Decrease", " Active players might be running out of things to do (Played ${0} vs ${1}, last two weeks vs weeks 2-4)"),
    NEW_PLAYER_JOIN_PLAYERS_GOOD("New Player Join Players, Yes", " New Players have players to play with when they join (${0} on average)"),
    NEW_PLAYER_JOIN_PLAYERS_BAD("New Player Join Players, No", " New Players may not have players to play with when they join (${0} on average)"),
    NEW_PLAYER_STICKINESS("New Player Stickiness", " ${0} of new players have stuck around (${1}/${2})"),
    TPS_ABOVE_LOW_THERSHOLD("TPS Above Low Threshold", " Average TPS was above Low Threshold ${0} of the time"),
    TPS_LOW_DIPS("TPS Low Dips", " Average TPS dropped below Low Threshold (${0}) ${1} times"),
    DOWNTIME("Downtime", " Total Server downtime (No Data) was ${0}"),
    NO_SERVERS_INACCURACY("No Servers Inaccuracy", " No Bukkit/Sponge servers to gather session data - These measures are inaccurate."),
    SINGLE_SERVER_INACCURACY("Single Servers Inaccuracy", " Single Bukkit/Sponge server to gather session data."),
    PLAYER_VISIT_PER_SERVER("Player Visit Server", " players visit on servers per day/server on average."),
    PLAYER_REGISTER_PER_SERVER("Player Register Server", " players register on servers per day/server on average."),
    PLAYER_PLAY_ON_NETWORK("Player Play on Network", " players played on the network:");

    private final String identifier;
    private final String defaultValue;

    HealthInfoLang(String identifier, String defaultValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "Health - " + identifier;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}