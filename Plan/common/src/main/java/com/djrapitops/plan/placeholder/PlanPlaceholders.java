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
package com.djrapitops.plan.placeholder;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PlanPlaceholders {

    private static final Map<String, BiFunction<PlayerContainer, List<String>, Serializable>> placeholders = new HashMap<>();
    private static final Map<String, Function<List<String>, Serializable>> staticPlaceholders = new HashMap<>();

    private static final Map<String, BiFunction<String, PlayerContainer, Serializable>> rawHandlers = new HashMap<>();

    private static DBSystem dbSystem;

    public static void init(PlanSystem system) {
        dbSystem = system.getDatabaseSystem();

        PlanConfig config = system.getConfigSystem().getConfig();
        ServerInfo serverInfo = system.getServerInfo();
        Formatters formatters = system.getDeliveryUtilities().getFormatters();

        new ServerPlaceHolders(dbSystem, serverInfo, formatters).register();
        new OperatorPlaceholders(dbSystem, serverInfo).register();
        new WorldTimePlaceHolder(dbSystem, serverInfo, formatters).register();
        new SessionPlaceHolder(config, dbSystem, serverInfo, formatters).register();
        new PlayerPlaceHolder(dbSystem, serverInfo, formatters).register();
    }

    public static void registerStatic(String name, Supplier<Serializable> loader) {
        registerStatic(name, a -> loader.get());
    }

    public static void registerStatic(String name, Function<List<String>, Serializable> loader) {
        staticPlaceholders.put(name, loader);
    }

    public static void register(String name, Function<PlayerContainer, Serializable> loader) {
        register(name, (p, a) -> loader.apply(p));
    }

    public static void register(String name, BiFunction<PlayerContainer, List<String>, Serializable> loader) {
        placeholders.put(name, loader);
    }

    public static void registerRaw(String name, BiFunction<String, PlayerContainer, Serializable> loader) {
        rawHandlers.put(name, loader);
    }

    public static Map<String, BiFunction<PlayerContainer, List<String>, Serializable>> getPlaceholders() {
        return placeholders;
    }

    public static Map<String, Function<List<String>, Serializable>> getStaticPlaceholders() {
        return staticPlaceholders;
    }

    public static String onPlaceholderRequest(UUID uuid, String placeholder, List<String> parameters) {
        PlayerContainer p;

        if (uuid != null) {
            p = dbSystem.getDatabase().query(ContainerFetchQueries.fetchPlayerContainer(uuid));
            SessionCache.getCachedSession(uuid).ifPresent(session -> p.putRawData(PlayerKeys.ACTIVE_SESSION, session));
        } else {
            p = null;
        }

        return onPlaceholderRequest(p, placeholder, parameters);
    }

    /**
     * Look up the placeholder and check if it is registered.
     *
     * @param p           the player who is viewing the placeholder
     * @param placeholder the placeholder to look up to.
     * @param parameters  additional placeholder parameters
     * @return the value of the placeholder if found, or empty {@link String} if no
     * value found but the placeholder is registered,
     * otherwise {@code null}
     * @throws Exception if any error occurs
     */
    public static String onPlaceholderRequest(PlayerContainer p, String placeholder, List<String> parameters) {
        for (Entry<String, BiFunction<String, PlayerContainer, Serializable>> entry : rawHandlers.entrySet()) {
            if (placeholder.startsWith(entry.getKey())) {
                return Objects.toString(entry.getValue().apply(placeholder, p));
            }
        }

        Function<List<String>, Serializable> staticLoader = staticPlaceholders.get(placeholder);

        if (staticLoader != null) {
            return Objects.toString(staticLoader.apply(parameters));
        }

        if (p != null) {
            BiFunction<PlayerContainer, List<String>, Serializable> loader = placeholders.get(placeholder);

            if (loader != null) {
                return Objects.toString(loader.apply(p, parameters));
            }
        }

        return null;
    }
}
