package com.djrapitops.plan.system.locale.lang;

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
    NEW_PLAYERS("New Players"),
    LAST_PEAK("Last Peak"),
    ALL_TIME_PEAK("All Time Peak"),
    SERVER_INFORMATION("SERVER INFORMATION"),
    USER_INFORMATION("USER INFORMATION"),
    RECENT_LOGINS("RECENT LOGINS"),
    UNIQUE_PLAYERS("UNIQUE PLAYERS"),
    CALENDAR("CALENDAR"),
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
    LOW_TPS_SPIKES("Low TPS Spikes"),
    LOADED_ENTITIES("Loaded Entities"),
    LOADED_CHUNKS("Loaded Chunks"),
    ENTITIES("Entities"),
    CHUNKS("Chunks"),
    AVG("AVG"),
    PLAYER_LIST("Player List"),
    NAME("Name"),
    REGISTERED_TEXT("Registered"),
    GEOLOCATION_TEXT("Geolocation"),
    COUNTRY("Country"),
    COMMNAND_USAGE("Command Usage"),
    USED_COMMANDS("Used Commands"),
    UNIQUE_TEXT("Unique");

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