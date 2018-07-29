package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} implementation for player.html replacement values.
 *
 * @author Rsl1122
 */
public enum PlayerPageLang implements Lang {
    PLAYER_KILLS("Player Kills"),
    MOB_KILLS("Mob Kills"),
    DEATHS("Deaths"),
    SESSIONS("Sessions"),
    TOTAL("Total"),
    PLAYTIME("Playtime"),
    ACTIVE("Active"),
    MEDIAN("Median"),
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
    CALENDAR_TEXT("Calendar"),
    MOST_RECENT("Most Recent"),
    WORLD(" World"),
    PREFERENCE("Preference"),
    LAST_30_DAYS("LAST 30 DAYS"),
    LAST_7_DAYS("LAST 7 DAYS"),
    LAST_24_HOURS("LAST 24 HOURS"),
    SERVERS("Servers"),
    OVERVIEW("OVERVIEW"),
    PLAYER_CAUSED_DEATHS("Player caused Deaths"),
    MOB_CAUSED_DEATHS("Mob caused Deaths"),
    TIME("Time"),
    KILLED("Killed"),
    WITH("With"),
    KILLED_BY("Killed by");

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