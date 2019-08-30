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
package com.djrapitops.plan.system.settings.locale.lang;

/**
 * {@link Lang} implementation for player.html replacement values.
 *
 * @author Rsl1122
 */
public enum ServerPageLang implements Lang {
    SERVER_ANALYSIS("Server Analysis"),
    PLAYERS_ONLINE("PLAYERS ONLINE"),
    UNIQUE("UNIQUE"),
    NEW("NEW"),
    REGULAR("REGULAR"),
    TOTAL_PLAYERS("Total Players"),
    UNIQUE_PLAYERS_TEXT("Unique Players"),
    NEW_PLAYERS_TEXT("New Players"),
    PER_DAY("/ Day"),
    LAST_PEAK("Last Peak"),
    ALL_TIME_PEAK("All Time Peak"),
    SERVER_INFORMATION("SERVER INFORMATION"),
    USER_INFORMATION("USER INFORMATION"),
    RECENT_LOGINS("RECENT LOGINS"),
    ONLINE_ACTIVITY("ONLINE ACTIVITY"),
    UNIQUE_PLAYERS("UNIQUE PLAYERS"),
    CALENDAR("CALENDAR"),
    PUNCHCARD("PUNCHCARD"),
    UNIQUE_CALENDAR("Unique:"),
    NEW_CALENDAR("New:"),
    NEW_RETENTION("New Player Retention"),
    PREDICETED_RETENTION("Predicted Retention"),
    SERVER_HEALTH_ESTIMATE("Server Health Estimate"),
    LAST_30_DAYS_TEXT("Last 30 Days"),
    PLAYERBASE_DEVELOPMENT("Playerbase Development"),
    CURRENT_PLAYERBASE("Current Playerbase"),
    WORLD_PLAYTIME("World Playtime"),
    WORLD_LOAD("WORLD LOAD"),
    ALL("ALL"),
    LOW_TPS_SPIKES("Low TPS Spikes"),
    USAGE(" Usage"),
    LOADED_ENTITIES("Loaded Entities"),
    LOADED_CHUNKS("Loaded Chunks"),
    ENTITIES("Entities"),
    CHUNKS("Chunks"),
    AVG("AVG"),
    PLAYER_LIST("Player List"),
    NAME(" Name"),
    REGISTERED_TEXT("Registered"),
    GEOLOCATION_TEXT("Geolocation"),
    COUNTRY("Country"),
    COMMNAND_USAGE("Command Usage"),
    USED_COMMANDS("Used Commands"),
    UNIQUE_TEXT("Unique"),
    COMMAND(" Command"),
    TIMES_USED("Times Used"),
    FREE_DISK_SPACE("Free Disk Space"),
    DISK_SPACE("DISK SPACE"),
    ;

    private final String defaultValue;

    ServerPageLang(String defaultValue) {
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