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
package com.djrapitops.plan.delivery.domain.auth;

import com.djrapitops.plan.settings.locale.lang.Lang;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * List of web permissions.
 *
 * @author AuroraLS3
 */
public enum WebPermission implements Supplier<String>, Lang {
    PAGE("Controls what is visible on pages"),
    PAGE_NETWORK("See all of network page"),
    PAGE_NETWORK_OVERVIEW("See Network Overview -tab"),
    PAGE_NETWORK_OVERVIEW_NUMBERS("See Network Overview numbers"),
    PAGE_NETWORK_OVERVIEW_GRAPHS("See Network Overview graphs"),
    PAGE_NETWORK_OVERVIEW_GRAPHS_ONLINE("See Players Online graph"),
    PAGE_NETWORK_OVERVIEW_GRAPHS_DAY_BY_DAY("See Day by Day graph"),
    PAGE_NETWORK_OVERVIEW_GRAPHS_HOUR_BY_HOUR("See Hour by Hour graph"),
    PAGE_NETWORK_OVERVIEW_GRAPHS_CALENDAR("See Network calendar"),
    PAGE_NETWORK_SERVER_LIST("See list of servers"),
    PAGE_NETWORK_PLAYERBASE("See Playerbase Overview -tab"),
    PAGE_NETWORK_PLAYERBASE_OVERVIEW("See Playerbase Overview numbers"),
    PAGE_NETWORK_PLAYERBASE_GRAPHS("See Playerbase Overview graphs"),
    PAGE_NETWORK_SESSIONS("See Sessions tab"),
    PAGE_NETWORK_SESSIONS_OVERVIEW("See Session insights"),
    PAGE_NETWORK_SESSIONS_WORLD_PIE("See World Pie graph"),
    PAGE_NETWORK_SESSIONS_SERVER_PIE("See Server Pie graph"),
    PAGE_NETWORK_SESSIONS_LIST("See list of sessions"),
    PAGE_NETWORK_JOIN_ADDRESSES("See Join Addresses -tab"),
    PAGE_NETWORK_JOIN_ADDRESSES_GRAPHS("See Join Address graphs"),
    @Deprecated
    PAGE_NETWORK_JOIN_ADDRESSES_GRAPHS_PIE("See Latest Join Addresses graph", true),
    PAGE_NETWORK_JOIN_ADDRESSES_GRAPHS_TIME("See Join Addresses over time graph"),
    PAGE_NETWORK_RETENTION("See Player Retention -tab"),
    PAGE_NETWORK_GEOLOCATIONS("See Geolocations tab"),
    PAGE_NETWORK_GEOLOCATIONS_MAP("See Geolocations Map"),
    PAGE_NETWORK_GEOLOCATIONS_PING_PER_COUNTRY("See Ping Per Country table"),
    PAGE_NETWORK_PLAYERS("See Player list -tab"),
    PAGE_NETWORK_PERFORMANCE("See network Performance tab"),
    PAGE_NETWORK_PLUGIN_HISTORY("See Plugin History across the network"),
    PAGE_NETWORK_PLUGINS("See Plugins tab of Proxy"),

    PAGE_SERVER("See all of server page"),
    PAGE_SERVER_OVERVIEW("See Server Overview -tab"),
    PAGE_SERVER_OVERVIEW_NUMBERS("See Server Overview numbers"),
    PAGE_SERVER_OVERVIEW_PLAYERS_ONLINE_GRAPH("See Players Online graph"),
    PAGE_SERVER_ONLINE_ACTIVITY("See Online Activity -tab"),
    PAGE_SERVER_ONLINE_ACTIVITY_OVERVIEW("See Online Activity numbers"),
    PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS("See Online Activity graphs"),
    PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_DAY_BY_DAY("See Day by Day graph"),
    PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_HOUR_BY_HOUR("See Hour by Hour graph"),
    PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_PUNCHCARD("See Punchcard graph"),
    PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_CALENDAR("See Server calendar"),
    PAGE_SERVER_PLAYERBASE("See Playerbase Overview -tab"),
    PAGE_SERVER_PLAYERBASE_OVERVIEW("See Playerbase Overview numbers"),
    PAGE_SERVER_PLAYERBASE_GRAPHS("See Playerbase Overview graphs"),
    PAGE_SERVER_PLAYER_VERSUS("See PvP & PvE -tab"),
    PAGE_SERVER_PLAYER_VERSUS_OVERVIEW("See PvP & PvE numbers"),
    PAGE_SERVER_PLAYER_VERSUS_KILL_LIST("See Player kill and death lists"),
    PAGE_SERVER_PLAYERS("See Player list -tab"),
    PAGE_SERVER_SESSIONS("See Sessions tab"),
    PAGE_SERVER_SESSIONS_OVERVIEW("See Session insights"),
    PAGE_SERVER_SESSIONS_WORLD_PIE("See World Pie graph"),
    PAGE_SERVER_SESSIONS_LIST("See list of sessions"),
    PAGE_SERVER_JOIN_ADDRESSES("See Join Addresses -tab"),
    PAGE_SERVER_JOIN_ADDRESSES_GRAPHS("See Join Address graphs"),
    @Deprecated
    PAGE_SERVER_JOIN_ADDRESSES_GRAPHS_PIE("See Latest Join Addresses graph", true),
    PAGE_SERVER_JOIN_ADDRESSES_GRAPHS_TIME("See Join Addresses over time graph"),
    PAGE_SERVER_RETENTION("See Player Retention -tab"),
    PAGE_SERVER_GEOLOCATIONS("See Geolocations tab"),
    PAGE_SERVER_GEOLOCATIONS_MAP("See Geolocations Map"),
    PAGE_SERVER_GEOLOCATIONS_PING_PER_COUNTRY("See Ping Per Country table"),
    PAGE_SERVER_PERFORMANCE("See Performance tab"),
    PAGE_SERVER_PERFORMANCE_GRAPHS("See Performance graphs"),
    PAGE_SERVER_PERFORMANCE_GRAPHS_PLAYERS_ONLINE("See Players Online data in Performance graphs"),
    PAGE_SERVER_PERFORMANCE_GRAPHS_TPS("See TPS data in Performance graphs"),
    PAGE_SERVER_PERFORMANCE_GRAPHS_CPU("See CPU usage in Performance graphs"),
    PAGE_SERVER_PERFORMANCE_GRAPHS_RAM("See Memory usage in Performance graphs"),
    PAGE_SERVER_PERFORMANCE_GRAPHS_ENTITIES("See Entity count data in Performance graphs"),
    PAGE_SERVER_PERFORMANCE_GRAPHS_CHUNKS("See Chunk count data in Performance graphs"),
    PAGE_SERVER_PERFORMANCE_GRAPHS_DISK("See Disk Space usage Performance graphs"),
    PAGE_SERVER_PERFORMANCE_GRAPHS_PING("See Ping data in Performance graphs"),
    PAGE_SERVER_PERFORMANCE_OVERVIEW("See Performance numbers"),
    PAGE_SERVER_PLUGIN_HISTORY("See Plugin History"),
    PAGE_SERVER_PLUGINS("See Plugins -tabs of servers"),
    PAGE_SERVER_ALLOWLIST_BOUNCE("See list of Game allowlist bounces"),

    PAGE_PLAYER("See all of player page"),
    PAGE_PLAYER_OVERVIEW("See Player Overview -tab"),
    PAGE_PLAYER_SESSIONS("See Player Sessions -tab"),
    PAGE_PLAYER_VERSUS("See PvP & PvE -tab"),
    PAGE_PLAYER_SERVERS("See Servers -tab"),
    PAGE_PLAYER_PLUGINS("See Plugins -tabs"),

    ACCESS("Controls access to pages"),
    ACCESS_PLAYER("Allows accessing any /player pages"),
    ACCESS_PLAYER_SELF("Allows accessing own /player page"),
    ACCESS_RAW_PLAYER_DATA("Allows accessing /player/{uuid}/raw json data. Follows 'access.player' permissions."),
    // Restricting to specific servers: access.server.uuid
    ACCESS_SERVER("Allows accessing all /server pages"),
    ACCESS_NETWORK("Allows accessing /network page"),
    ACCESS_PLAYERS("Allows accessing /players page"),
    ACCESS_QUERY("Allows accessing /query and Query results pages"),
    ACCESS_ERRORS("Allows accessing /errors page"),
    ACCESS_DOCS("Allows accessing /docs page"),

    MANAGE_GROUPS("Allows modifying group permissions & Access to /manage/groups page"),
    MANAGE_USERS("Allows modifying what users belong to what group");

    private final String description;
    private final boolean deprecated;

    WebPermission(String description) {
        this(description, false);
    }

    WebPermission(String description, boolean deprecated) {
        this.description = description;
        this.deprecated = deprecated;
    }

    public String getPermission() {
        return StringUtils.lowerCase(name()).replace('_', '.');
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    @Override
    public String get() {
        return getPermission();
    }

    @Override
    public String getIdentifier() {
        return "HTML - Permission " + name();
    }

    @Override
    public String getKey() {
        return "html.manage.permission.description." + name().toLowerCase();
    }

    @Override
    public String getDefault() {
        return description;
    }

    public static WebPermission[] nonDeprecatedValues() {
        return Arrays.stream(values())
                .filter(Predicate.not(WebPermission::isDeprecated))
                .toArray(WebPermission[]::new);
    }

    public static Optional<WebPermission> findByPermission(String permission) {
        String name = StringUtils.upperCase(permission).replace('.', '_');
        try {
            return Optional.of(valueOf(name));
        } catch (IllegalArgumentException noSuchEnum) {
            return Optional.empty();
        }
    }

    public static boolean isDeprecated(String permission) {
        return findByPermission(permission).map(WebPermission::isDeprecated).orElse(false);
    }
}
