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
 * Lang enum for all text included in the javascript files.
 *
 * @author AuroraLS3
 */
public enum JSLang implements Lang {

    TEXT_PREDICTED_RETENTION("html.description.predictedNewPlayerRetention", "This value is a prediction based on previous players"),
    TEXT_NO_SERVERS("html.description.noServers", "No servers found in the database"),
    TEXT_SERVER_INSTRUCTIONS("html.description.noServersLong", "It appears that Plan is not installed on any game servers or not connected to the same database. See <a href=\"https://github.com/plan-player-analytics/Plan/wiki\">wiki</a> for Network tutorial."),
    TEXT_NO_SERVER("html.description.noServerOnlinActivity", "No server to display online activity for"),
    LABEL_REGISTERED_PLAYERS("html.label.registeredPlayers", "Registered Players"),
    LINK_SERVER_ANALYSIS("html.label.serverAnalysis", "Server Analysis"),
    LINK_QUICK_VIEW("html.label.quickView", "Quick view"),
    TEXT_FIRST_SESSION("html.label.firstSession", "First session"),
    LABEL_SESSION_ENDED("html.label.sessionEnded", " Ended"),
    LINK_PLAYER_PAGE("html.label.playerPage", "Player Page"),
    LABEL_NO_SESSION_KILLS("html.generic.none", "None"),
    // UNIT_ENTITIES("html.unit.entities", "Entities"),
    UNIT_CHUNKS("html.unit.chunks", "Chunks"),
    LABEL_RELATIVE_JOIN_ACTIVITY("html.label.relativeJoinActivity", "Relative Join Activity"),
    LABEL_DAY_OF_WEEK("html.label.dayOfweek", "Day of the Week"),
    LABEL_WEEK_DAYS("html.label.weekdays", "'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'"),

    QUERY_ARE_ACTIVITY_GROUP("html.query.filter.activity.text", "are in Activity Groups"),
    QUERY_JOINED_WITH_ADDRESS("html.query.filter.joinAddress.text", "joined with address"),
    QUERY_JOINED_FROM_COUNTRY("html.query.filter.country.text", "have joined from country"),
    QUERY_ARE_PLUGIN_GROUP("html.query.filter.pluginGroup.text", "are in ${plugin}'s ${group} Groups"),
    QUERY_OF_PLAYERS("html.query.filter.generic.start", "of Players who "),
    QUERY_AND("html.query.filter.generic.and", "and "),
    QUERY_PLAYED_BETWEEN("html.query.filter.playedBetween.text", "Played between"),
    QUERY_REGISTERED_BETWEEN("html.query.filter.registeredBetween.text", "Registered between"),
    QUERY_ZERO_RESULTS("html.query.results.none", "Query produced 0 results"),
    QUERY_RESULTS("html.query.results.title", "Query Results"),
    QUERY_RESULTS_MATCH("html.query.results.match", "matched ${resultCount} players"),
    QUERY_VIEW("html.query.filter.view", "  View:"),
    QUERY_ACTIVITY_OF_MATCHED_PLAYERS("html.query.title.activity", "Activity of matched players"),
    QUERY_ACTIVITY_ON("html.query.title.activityOnDate", "Activity on <span id=\"activity-date\"></span>"),
    QUERY_ARE("html.query.generic.are", "`are`"),
    QUERY_SESSIONS_WITHIN_VIEW("html.query.title.sessionsWithinView", "Sessions within view"),

    FILTER_GROUP("html.query.filter.pluginGroup.name", "Group: "),
    FILTER_ALL_PLAYERS("html.query.filter.generic.allPlayers", "All players"),
    FILTER_ACTIVITY_INDEX_NOW("html.query.filter.title.activityGroup", "Current activity group"),
    FILTER_BANNED("html.query.filter.banStatus.name", "Ban status"),
    FILTER_OPS("html.query.filter.operatorStatus.name", "Operator status");

    private final String key;
    private final String defaultValue;

    JSLang(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "HTML - " + name();
    }

    @Override
    public String getKey() { return key; }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}