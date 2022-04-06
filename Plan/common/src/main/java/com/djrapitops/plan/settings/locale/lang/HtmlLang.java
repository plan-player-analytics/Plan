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
package com.djrapitops.plan.settings.locale.lang;

/**
 * Lang enum for all text included in the html files.
 *
 * @author AuroraLS3
 */
public enum HtmlLang implements Lang {

    TITLE_NETWORK("html.label.network", "Network"),
    // Sidebar
    SIDE_INFORMATION("html.label.information", "INFORMATION"), // Nav group title
    SIDE_NETWORK_OVERVIEW("html.label.networkOverview", "Network Overview"),
    SIDE_SERVERS("html.label.servers", "Servers"),
    SIDE_OVERVIEW("html.label.overview", "Overview"),
    SIDE_SESSIONS("html.label.sessions", "Sessions"),
    SIDE_PLAYERBASE("html.label.playerbase", "Playerbase"),
    SIDE_PLAYER_LIST("html.label.playerList", "Player List"),
    SIDE_GEOLOCATIONS("html.label.geolocations", "Geolocations"),
    SIDE_LINKS("html.label.links", "LINKS"),
    SIDE_PERFORMANCE("html.label.performance", "Performance"),
    QUERY_MAKE("html.label.query", "Make a query"),
    UNIT_NO_DATA("generic.noData", "No Data"), // Generic
    // Modals
    TITLE_THEME_SELECT("html.label.themeSelect", "Theme Select"),
    LINK_NIGHT_MODE("html.button.nightMode", "Night Mode"),
    TEXT_PLUGIN_INFORMATION("html.modal.info.text", "Information about the plugin"),
    TEXT_LICENSED_UNDER("html.modal.info.license", "Player Analytics is developed and licensed under"),
    LINK_WIKI("html.modal.info.wiki", "Plan Wiki, Tutorials & Documentation"),
    LINK_ISSUES("html.modal.info.bugs", "Report Issues"),
    LINK_DISCORD("html.modal.info.discord", "General Support on Discord"),
    AND_BUG_REPORTERS("html.modal.info.contributors.bugreporters", "& Bug reporters!"),
    TEXT_DEVELOPED_BY("html.modal.info.developer", "is developed by"),
    TEXT_CONTRIBUTORS_THANKS("html.modal.info.contributors.text", "In addition following <span class=\"col-plan\">awesome people</span> have contributed:"),
    TEXT_CONTRIBUTORS_CODE("html.modal.info.contributors.code", "code contributor"),
    TEXT_CONTRIBUTORS_LOCALE("html.modal.info.contributors.translator", "translator"),
    TEXT_CONTRIBUTORS_MONEY("html.modal.info.contributors.donate", "Extra special thanks to those who have monetarily supported the development."),
    TEXT_METRICS("html.modal.info.metrics", "bStats Metrics"),
    TITLE_VERSION("html.modal.version.title", "Version"),
    TITLE_IS_AVAILABLE("html.modal.version.available", "is Available"),
    TEXT_VERSION("html.modal.version.text", "A new version has been released and is now available for download."),
    TEXT_DEV_VERSION("html.modal.version.dev", "This version is a DEV release."),
    LINK_CHANGELOG("html.modal.version.changelog", "View Changelog"),
    LINK_DOWNLOAD("html.modal.version.download", "Download"),
    // Network overview tab
    TITLE_GRAPH_NETWORK_ONLINE_ACTIVITY("html.label.networkOnlineActivity", "Network Online Activity"),
    TITLE_GRAPH_DAY_BY_DAY("html.label.dayByDay", "Day by Day"),
    TITLE_GRAPH_HOUR_BY_HOUR("html.label.hourByHour", "Hour by Hour"),
    UNIT_THE_PLAYERS("html.unit.players", "Players"),
    TITLE_LAST_24_HOURS("html.label.last24hours", "Last 24 hours"),
    TITLE_LAST_7_DAYS("html.label.last7days", "Last 7 days"),
    TITLE_LAST_30_DAYS("html.label.last30days", "Last 30 days"),
    LABEL_UNIQUE_PLAYERS("html.label.uniquePlayers", "Unique Players"),
    LABEL_NEW_PLAYERS("html.label.newPlayers", "New Players"),
    LABEL_REGULAR_PLAYERS("html.label.regularPlayers", "Regular Players"),
    LABEL_TOTAL_PLAYERS("html.label.totalPlayers", "Total Players"),
    TITLE_NETWORK_AS_NUMBERS("html.label.networkAsNumbers", "Network as Numbers"),
    LABEL_PLAYERS_ONLINE("html.label.playersOnline", "Players Online"),
    LABEL_TOTAL_PLAYTIME("html.label.totalPlaytime", "Total Playtime"),
    LABEL_PLAYTIME("html.label.playtime", "Playtime"),
    LABEL_ACTIVE_PLAYTIME("html.label.activePlaytime", "Active Playtime"),
    LABEL_LAST_PEAK("html.label.lastPeak", "Last Peak"),
    LABEL_BEST_PEAK("html.label.bestPeak", "Best Peak"),
    LABEL_AVG_PLAYTIME("html.label.averagePlaytime", "Average Playtime"),
    LABEL_AVG_SESSIONS("html.label.averageSessions", "Average Sessions"),
    LABEL_AVG_ACTIVE_PLAYTIME("html.label.averageActivePlaytime", "Average Active Playtime"),
    LABEL_AVG_AFK_TIME("html.label.averageAfkTime", "Average AFK Time"),
    LABEL_PER_PLAYER("html.label.perPlayer", "/ Player"),
    LABEL_AVG_SESSION_LENGTH("html.label.averageSessionLength", "Average Session Length"),
    TITLE_WEEK_COMPARISON("html.label.weekComparison", "Week Comparison"),
    TITLE_TRENDS("html.label.trends30days", "Trends for 30 days"),
    TITLE_TREND("html.label.trend", "Trend"),
    COMPARING_7_DAYS("html.label.comparing7days", "Comparing 7 days"),
    // Servers tab
    TITLE_ONLINE_ACTIVITY("html.label.onlineActivity", "Online Activity"),
    TITLE_30_DAYS("html.label.thirtyDays", "30 days"),
    TITLE_AS_NUMBERS("html.label.asNumbers", "as Numbers"),
    LABEL_AVG_TPS("html.label.averageTps", "Average TPS"),
    LABEL_AVG_ENTITIES("html.label.averageEntities", "Average Entities"),
    LABEL_AVG_CHUNKS("html.label.averageChunks", "Average Chunks"),
    LABEL_LOW_TPS("html.label.lowTpsSpikes", "Low TPS Spikes"),
    LABEL_DOWNTIME("html.label.downtime", "Downtime"),
    // Sessions tab #tab-sessions-overview
    TITLE_RECENT_SESSIONS("html.label.recentSessions", "Recent Sessions"),
    TITLE_PLAYER("html.label.player", "Player"),
    TITLE_SESSION_START("html.label.sessionStart", "Session Started"),
    TITLE_LENGTH("html.label.length", " Length"),
    TITLE_SERVER("html.label.server", "Server"), // Can cause issue with datatables.js
    TITLE_MOST_PLAYED_WORLD("html.label.mostPlayedWorld", "Most played World"),
    TEXT_CLICK_TO_EXPAND("html.text.clickToExpand", "Click to expand"),
    TITLE_SERVER_PLAYTIME_30("html.label.serverPlaytime30days", "Server Playtime for 30 days"),
    TITLE_INSIGHTS("html.label.insights30days", "Insights for 30 days"),
    LABEL_AFK_TIME("html.label.afkTime", "AFK Time"),
    LABEL_AFK("html.label.afk", "AFK"),
    // Playerbase overview tab #tab-playerbase-overview
    TITLE_PLAYERBASE_OVERVIEW("html.label.playerbaseOverview", "Playerbase Overview"),
    TITLE_PLAYERBASE_DEVELOPMENT("html.label.playerbaseDevelopment", "Playerbase development"),
    TITLE_CURRENT_PLAYERBASE("html.label.currentPlayerbase", "Current Playerbase"),
    TITLE_JOIN_ADDRESSES("html.label.joinAddresses", "Join Addresses"),
    COMPARING_60_DAYS("html.text.comparing30daysAgo", "Comparing 30d ago to Now"),
    TITLE_30_DAYS_AGO("html.label.thirtyDaysAgo", "30 days ago"),
    TITLE_NOW("html.label.now", "Now"),
    LABEL_PER_REGULAR_PLAYER("html.label.perRegularPlayer", "/ Regular Player"),
    LABEL_NEW("html.label.new", "New"),
    LABEL_REGULAR("html.label.regular", "Regular"),
    LABEL_INACTIVE("html.label.inactive", "Inactive"),
    SIDE_TO_MAIN_PAGE("html.label.toMainPage", "to main page"),
    // Geolocations tab
    TITLE_CONNECTION_INFO("html.label.connectionInfo", "Connection Information"),
    TITLE_COUNTRY("html.label.country", "Country"),
    TITLE_AVG_PING("html.label.averagePing", "Average Ping"),
    TITLE_WORST_PING("html.label.worstPing", "Worst Ping"),
    TITLE_BEST_PING("html.label.bestPing", "Best Ping"),
    TEXT_NO_EXTENSION_DATA("html.text.noExtensionData", "No Extension Data"),
    // Server page
    LINK_BACK_NETWORK("html.label.networkPage", "Network page"),
    SIDE_PVP_PVE("html.label.pvpPve", "PvP & PvE"),
    LABEL_RETENTION("html.label.newPlayerRetention", "New Player Retention"),
    DESCRIBE_RETENTION_PREDICTION("html.description.newPlayerRetention", "This value is a prediction based on previous players."),
    TITLE_SERVER_AS_NUMBERS("html.label.serverAsNumberse", "Server as Numbers"),
    TITLE_ONLINE_ACTIVITY_AS_NUMBERS("html.label.onlineActivityAsNumbers", "Online Activity as Numbers"),
    COMPARING_15_DAYS("html.text.comparing15days", "Comparing 15 days"),
    TITLE_GRAPH_PUNCHCARD("html.label.punchcard30days", "Punchcard for 30 Days"),
    LABEL_ONLINE_FIRST_JOIN("html.label.onlineOnFirstJoin", "Players online on first join"),
    LABEL_FIRST_SESSION_LENGTH("html.label.firstSessionLength", "First session length"),
    LABEL_LONE_JOINS("html.label.loneJoins", "Lone joins"),
    LABEL_LONE_NEW_JOINS("html.label.loneNewbieJoins", "Lone newbie joins"),
    LABEL_MOST_ACTIVE_GAMEMODE("html.label.mostActiveGamemode", "Most Active Gamemode"),
    LABEL_SERVER_OCCUPIED("html.label.serverOccupied", "Server occupied"),
    TITLE_PVP_PVE_NUMBERS("html.label.pvpPveAsNumbers", "PvP & PvE as Numbers"),
    LABEL_1ST_WEAPON("html.label.deadliestWeapon", "Deadliest PvP Weapon"),
    LABEL_2ND_WEAPON("html.label.secondDeadliestWeapon", "2nd PvP Weapon"),
    LABEL_3RD_WEAPON("html.label.thirdDeadliestWeapon", "3rd PvP Weapon"),
    LABEL_AVG_KDR("html.label.averageKdr", "Average KDR"),
    LABEL_PLAYER_KILLS("html.label.playerKills", "Player Kills"),
    LABEL_AVG_MOB_KDR("html.label.averageMobKdr", "Average Mob KDR"),
    LABEL_MOB_KILLS("html.label.mobKills", "Mob Kills"),
    LABEL_MOB_DEATHS("html.label.mobDeaths", "Mob Caused Deaths"),
    LABEL_DEATHS("html.label.deaths", "Deaths"),
    TITLE_RECENT_KILLS("html.label.recentKills", "Recent Kills"),
    TITLE_ALL("html.label.all", "All"),
    TITLE_TPS("html.label.tps", "TPS"),
    TITLE_CPU_RAM("html.label.cpuRam", "CPU & RAM"),
    TITLE_WORLD("html.label.world", "World Load"),
    TITLE_PING("html.label.ping", "Ping"),
    TITLE_DISK("html.label.disk", "Disk Space"),
    LABEL_AVG("html.label.average", "Average"),
    TITLE_PERFORMANCE_AS_NUMBERS("html.label.performanceAsNumbers", "Performance as Numbers"),
    LABEL_SERVER_DOWNTIME("html.label.serverDowntime", "Server Downtime"),
    LABEL_DURING_LOW_TPS("html.label.duringLowTps", "During Low TPS Spikes:"),
    TEXT_NO_LOW_TPS("html.text.noLowTps", "No low tps spikes"),
    // Player Page
    TITLE_SEEN_NICKNAMES("html.label.seenNicknames", "Seen Nicknames"),
    LABEL_LAST_SEEN("html.label.lastSeen", "Last Seen"),
    TITLE_LAST_CONNECTED("html.label.lastConnected", "Last Connected"),
    LABEL_PLAYER_DEATHS("html.label.playerDeaths", "Player Caused Deaths"),
    TITLE_PVP_KILLS("html.label.recentPvpKills", "Recent PvP Kills"),
    TITLE_PVP_DEATHS("html.label.recentPvpDeaths", "Recent PvP Deaths"),
    TITLE_SERVER_PLAYTIME("html.label.serverPlaytime", "Server Playtime"),
    LINK_BACK_SERVER("html.label.serverPage", "Server page"),
    SIDE_SERVERS_TITLE("html.label.serversTitle", "SERVERS"),
    // Were missing
    TITLE_SERVER_OVERVIEW("html.label.serverOverview", "Server Overview"),
    TITLE_ONLINE_ACTIVITY_OVERVIEW("html.label.playersOnlineOverview", "Online Activity Overview"),
    PER_DAY("html.label.perDay", "/ Day"),
    TITLE_WORLD_PLAYTIME("html.label.worldPlaytime", "World Playtime"),
    TITLE_PLAYER_OVERVIEW("html.label.playerOverview", "Player Overview"),
    LABEL_LONGEST_SESSION("html.label.longestSession", "Longest Session"),
    LABEL_REGISTERED("html.label.registered", "Registered"),
    TITLE_TITLE_PLAYER_PUNCHCARD("html.label.punchcard", "Punchcard"),
    TITLE_ALL_TIME("html.label.allTime", "All Time"),
    LABEL_NAME("html.label.name", "Name"),
    // React
    LABEL_TITLE_SESSION_CALENDAR("html.label.sessionCalendar", "Session Calendar"),
    LABEL_TITLE_SERVER_CALENDAR("html.label.serverCalendar", "Server Calendar"),
    LABEL_LABEL_JOIN_ADDRESS("html.label.joinAddress", "Join Address"),
    LABEL_LABEL_SESSION_MEDIAN("html.label.medianSessionLength", "Median Session Length"),
    LABEL_LABEL_KDR("html.label.kdr", "KDR"),
    LABEL_TITLE_INSIGHTS("html.label.insights", "Insights"),
    // ----------------------------------
    // OLD
    // ----------------------------------
    NAV_PLUGINS("html.label.plugins", "Plugins"),
    PLAYERS_TEXT("html.label.players", "Players"),
    TOTAL_PLAYERS("html.label.totalPlayersOld", "Total Players"),
    UNIQUE_CALENDAR("html.calendar.unique", "Unique:"),
    NEW_CALENDAR("html.calendar.new", "New:"),
    SESSION("html.label.session", "Session"),
    KILLED("html.label.killed", "Killed"),
    LABEL_LOADED_ENTITIES("html.label.loadedEntities", "Loaded Entities"),
    LABEL_LOADED_CHUNKS("html.label.loadedChunks", "Loaded Chunks"),
    LABEL_ENTITIES("html.label.entities", "Entities"),
    LABEL_FREE_DISK_SPACE("html.label.diskSpace", "Free Disk Space"),
    ONLINE("html.value.online", " Online"),
    OFFLINE("html.value.offline", " Offline"),
    LABEL_TIMES_KICKED("html.label.timesKicked", "Times Kicked"),
    TOTAL_ACTIVE_TEXT("html.label.totalActive", "Total Active"),
    TOTAL_AFK("html.label.totalAfk", "Total AFK"),
    LABEL_SESSION_MEDIAN("html.label.sessionMedian", "Session Median"),
    LABEL_ACTIVITY_INDEX("html.label.activityIndex", "Activity Index"),
    INDEX_ACTIVE("html.label.active", "Active"),
    INDEX_VERY_ACTIVE("html.label.veryActive", "Very Active"),
    INDEX_REGULAR("html.label.indexRegular", "Regular"),
    INDEX_IRREGULAR("html.label.irregular", "Irregular"),
    INDEX_INACTIVE("html.label.indexInactive", "Inactive"),
    LABEL_FAVORITE_SERVER("html.label.favoriteServer", "Favorite Server"),
    LABEL_NICKNAME("html.label.nickname", "Nickname"),
    LOCAL_MACHINE("html.value.localMachine", "Local Machine"),
    TITLE_CALENDAR("html.label.calendar", " Calendar"),
    LABEL_OPERATOR("html.label.operator", "Operator"),
    LABEL_BANNED("html.label.banned", "Banned"),
    LABEL_MOB_KDR("html.label.mobKdr", "Mob KDR"),
    WITH("html.value.with", "<th>With"),
    NO_KILLS("html.value.noKills", "No Kills"),
    LABEL_MAX_FREE_DISK("html.label.maxFreeDisk", "Max Free Disk"),
    LABEL_MIN_FREE_DISK("html.label.minFreeDisk", "Min Free Disk"),
    LABEL_CURRENT_UPTIME("html.label.currentUptime", "Current Uptime"),

    LOGIN_LOGIN("html.login.login", "Login"),
    LOGIN_LOGOUT("html.login.logout", "Logout"),
    LOGIN_USERNAME("html.login.username", "\"Username\""),
    LOGIN_PASSWORD("html.login.password", "\"Password\""),
    LOGIN_FORGOT_PASSWORD("html.login.forgotPassword", "Forgot Password?"),
    LOGIN_CREATE_ACCOUNT("html.login.register", "Create an Account!"),
    LOGIN_FORGOT_PASSWORD_INSTRUCTIONS_1("html.login.forgotPassword1", "Forgot password? Unregister and register again."),
    LOGIN_FORGOT_PASSWORD_INSTRUCTIONS_2("html.login.forgotPassword2", "Use the following command in game to remove your current user:"),
    LOGIN_FORGOT_PASSWORD_INSTRUCTIONS_3("html.login.forgotPassword3", "Or using console:"),
    LOGIN_FORGOT_PASSWORD_INSTRUCTIONS_4("html.login.forgotPassword4", "After using the command, "),
    LOGIN_FAILED("html.login.failed", "Login failed: "),
    REGISTER("html.register.register", "Register"),
    REGISTER_CREATE_USER("html.register.createNewUser", "Create a new user"),
    REGISTER_USERNAME_TIP("html.register.usernameTip", "Username can be up to 50 characters."),
    REGISTER_PASSWORD_TIP("html.register.passwordTip", "Password should be more than 8 characters, but there are no limitations."),
    REGISTER_HAVE_ACCOUNT("html.register.login", "Already have an account? Login!"),
    REGISTER_USERNAME_LENGTH("html.register.error.usernameLength", "Username can be up to 50 characters, yours is "),
    REGISTER_SPECIFY_USERNAME("html.register.error.noUsername", "You need to specify a Username"),
    REGISTER_SPECIFY_PASSWORD("html.register.error.noPassword", "You need to specify a Password"),
    REGISTER_COMPLETE("html.register.completion", "Complete Registration"),
    REGISTER_COMPLETE_INSTRUCTIONS_1("html.register.completion1", "You can now finish registering the user."),
    REGISTER_COMPLETE_INSTRUCTIONS_2("html.register.completion2", "Code expires in 15 minutes"),
    REGISTER_COMPLETE_INSTRUCTIONS_3("html.register.completion3", "Use the following command in game to finish registration:"),
    REGISTER_COMPLETE_INSTRUCTIONS_4("html.register.completion4", "Or using console:"),
    REGISTER_FAILED("html.register.error.failed", "Registration failed: "),
    REGISTER_CHECK_FAILED("html.register.error.checkFailed", "Checking registration status failed: "),

    QUERY_PERFORM_QUERY("html.query.performQuery", "Perform Query!"),
    QUERY_LOADING_FILTERS("html.query.filters.loading", "Loading filters.."),
    QUERY_ADD_FILTER("html.query.filters.add", "Add a filter.."),
    QUERY_TIME_TO("html.query.label.to", ">to</label>"),
    QUERY_TIME_FROM("html.query.label.from", ">from</label>"),
    QUERY_SHOW_VIEW("html.query.label.view", "Show a view"),
    QUERY("html.query.title.text", "Query<"),
    QUERY_MAKE_ANOTHER("html.query.label.makeAnother", "Make another query"),

    WARNING_NO_GAME_SERVERS("html.description.noGameServers", "Some data requires Plan to be installed on game servers."),
    WARNING_NO_GEOLOCATIONS("html.description.noGeolocations", "Geolocation gathering needs to be enabled in the config (Accept GeoLite2 EULA)."),
    WARNING_NO_SPONGE_CHUNKS("html.description.noSpongeChunks", "Chunks unavailable on Sponge");

    private final String key;
    private final String defaultValue;

    HtmlLang(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "HTML - " + name();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }

}