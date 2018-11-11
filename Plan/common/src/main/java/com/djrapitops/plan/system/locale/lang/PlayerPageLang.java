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
 * {@link Lang} implementation for player.html replacement values.
 *
 * @author Rsl1122
 */
public enum PlayerPageLang implements Lang {
    ONLINE(" Online"),
    OFFLINE(" Offline"),
    TIMES_KICKED("Times Kicked"),
    PLAYER_KILLS("Player Kills"),
    MOB_KILLS("Mob Kills"),
    DEATHS("Deaths"),
    SESSIONS("Sessions"),
    TOTAL_PLAYTIME("Total Playtime"),
    PLAYTIME("Playtime"),
    TOTAL_ACTIVE_TEXT("Total Active"),
    TOTAL_AFK("Total AFK"),
    SESSION_MEDIAN("Session Median"),
    LONGEST("Longest"),
    SESSION("Session"),
    ACTIVITY_INDEX("Activity Index"),
    INDEX_ACTIVE("Active"),
    INDEX_VERY_ACTIVE("Very Active"),
    INDEX_REGULAR("Regular"),
    INDEX_IRREGULAR("Irregular"),
    INDEX_INACTIVE("Inactive"),
    FAVORITE_SERVER("Favorite Server"),
    REGISTERED("REGISTERED"),
    LAST_SEEN("LAST SEEN"),
    PUNCH_CARD("Punchcard"),
    SEEN_NICKNAMES("Seen Nicknames"),
    NICKNAME("Nickname"),
    SERVER("Server"),
    LAST_SEEN_TEXT("Last Seen"),
    CONNECTION_INFORMATION("Connection Information"),
    IP_ADDRESS("IP-address"),
    GEOLOCATION("Geolocation"),
    LAST_CONNECTED("Last Connected"),
    LOCAL_MACHINE("Local Machine"),
    CALENDAR_TEXT(" Calendar"),
    MOST_RECENT_SESSIONS("Most Recent Sessions"),
    SESSION_ENDED("Session Ended"),
    SESSION_LENGTH("Session Lenght"),

    WORLD(" World"),
    SERVER_PREFERENCE("Server Preference"),
    LAST_30_DAYS("LAST 30 DAYS"),
    LAST_7_DAYS("LAST 7 DAYS"),
    LAST_24_HOURS("LAST 24 HOURS"),
    SERVERS("Servers"),
    OPERATOR("Operator"),
    BANNED("Banned"),
    OVERVIEW("OVERVIEW"),
    PLAYER_CAUSED_DEATHS("Player caused Deaths"),
    MOB_CAUSED_DEATHS("Mob caused Deaths"),
    MOB_KDR("Mob KDR"),
    TIME(" Time"),
    KILLED("Killed"),
    WITH("<th>With"),
    KILLED_BY("Killed by"),
    NO_KILLS("No Kills"),
    NO_PLAYER_CAUSED_DEATHS("No Player caused Deaths");

    private final String defaultValue;

    PlayerPageLang(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "HTML - " + name();
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}