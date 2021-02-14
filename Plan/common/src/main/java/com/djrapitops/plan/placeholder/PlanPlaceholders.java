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

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Registry for all placeholders.
 *
 * @see ServerPlaceHolders Placeholders about the current server
 * @see OperatorPlaceholders Placeholders about operators of the current server
 * @see PlayerPlaceHolders Placeholders about the current player on the whole network
 * @see SessionPlaceHolders Placeholders about the player on the current server
 * @see WorldTimePlaceHolders Placeholders about the world times of current player on the current server
 */
@Singleton
public final class PlanPlaceholders {

    private final Map<String, PlayerPlaceholderLoader> playerPlaceholders;
    private final Map<String, StaticPlaceholderLoader> staticPlaceholders;

    private final Map<String, BiFunction<String, PlayerContainer, Serializable>> rawHandlers;

    private final DBSystem dbSystem;

    @Inject
    public PlanPlaceholders(
            DBSystem dbSystem,
            Set<Placeholders> placeholderRegistries
    ) {
        this.dbSystem = dbSystem;

        this.playerPlaceholders = new HashMap<>();
        this.staticPlaceholders = new HashMap<>();
        this.rawHandlers = new HashMap<>();

        for (Placeholders registry : placeholderRegistries) {
            registry.register(this);
        }
    }

    public void registerStatic(String name, Supplier<Serializable> loader) {
        registerStatic(name, params -> loader.get());
    }

    public void registerStatic(String name, StaticPlaceholderLoader loader) {
        staticPlaceholders.put(name, loader);
    }

    public void register(String name, Function<PlayerContainer, Serializable> loader) {
        register(name, (player, params) -> loader.apply(player));
    }

    public void register(String name, PlayerPlaceholderLoader loader) {
        playerPlaceholders.put(name, loader);
    }

    public void registerRaw(String name, BiFunction<String, PlayerContainer, Serializable> loader) {
        rawHandlers.put(name, loader);
    }

    public Map<String, PlayerPlaceholderLoader> getPlaceholders() {
        return playerPlaceholders;
    }

    public Map<String, StaticPlaceholderLoader> getStaticPlaceholders() {
        return staticPlaceholders;
    }

    public String onPlaceholderRequest(UUID uuid, String placeholder, List<String> parameters) {
        PlayerContainer player;

        if (uuid != null) {
            player = dbSystem.getDatabase().query(ContainerFetchQueries.fetchPlayerContainer(uuid));
            SessionCache.getCachedSession(uuid).ifPresent(session -> player.putRawData(PlayerKeys.ACTIVE_SESSION, session));
        } else {
            player = null;
        }

        return onPlaceholderRequest(player, placeholder, parameters);
    }

    /**
     * Look up the placeholder and check if it is registered.
     *
     * @param player      the player who is viewing the placeholder
     * @param placeholder the placeholder to look up to.
     * @param parameters  additional placeholder parameters
     * @return the value of the placeholder if found, or empty {@link String} if no
     * value found but the placeholder is registered,
     * otherwise {@code null}
     */
    public String onPlaceholderRequest(PlayerContainer player, String placeholder, List<String> parameters) {
        for (Entry<String, BiFunction<String, PlayerContainer, Serializable>> entry : rawHandlers.entrySet()) {
            if (placeholder.startsWith(entry.getKey())) {
                return Objects.toString(entry.getValue().apply(placeholder, player));
            }
        }

        StaticPlaceholderLoader staticLoader = staticPlaceholders.get(placeholder);
        if (staticLoader != null) {
            return Objects.toString(staticLoader.apply(parameters));
        }

        PlayerPlaceholderLoader loader = playerPlaceholders.get(placeholder);
        if (loader != null && player != null) {
            return Objects.toString(loader.apply(player, parameters));
        }

        return null;
    }

    public interface PlayerPlaceholderLoader extends BiFunction<PlayerContainer, List<String>, Serializable> {}

    public interface StaticPlaceholderLoader extends Function<List<String>, Serializable> {}
}
