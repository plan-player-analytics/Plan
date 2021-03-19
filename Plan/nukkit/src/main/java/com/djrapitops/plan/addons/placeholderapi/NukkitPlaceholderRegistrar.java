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
package com.djrapitops.plan.addons.placeholderapi;

import cn.nukkit.Player;
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI;
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters;
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters.Parameter;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.placeholder.PlanPlaceholders;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Placeholder expansion used to provide data from Plan on Nukkit.
 *
 * @author developStorm
 */
@Singleton
public class NukkitPlaceholderRegistrar {

    private final PlanPlaceholders placeholders;
    private final PlanSystem system;
    private final ErrorLogger errorLogger;

    @Inject
    public NukkitPlaceholderRegistrar(
            PlanPlaceholders placeholders,
            PlanSystem system,
            ErrorLogger errorLogger
    ) {
        this.placeholders = placeholders;
        this.system = system;
        this.errorLogger = errorLogger;
    }

    public void register() {
        PlaceholderAPI api = PlaceholderAPI.getInstance();
        placeholders.getPlaceholders().forEach((name, loader) -> api.builder(name, Serializable.class)
                .visitorLoader(options -> {
                    try {
                        return loader.apply(
                                getPlayer(options.getPlayer()),
                                getPlaceholderParameterValues(options.getParameters())
                        );
                    } catch (Exception e) {
                        errorLogger.warn(e, ErrorContext.builder().related("Registering PlaceholderAPI").build());
                        return null;
                    }
                }).build()
        );

        placeholders.getStaticPlaceholders().forEach((name, loader) -> api.builder(name, Serializable.class)
                .loader(options -> {
                    try {
                        return loader.apply(
                                getPlaceholderParameterValues(options.getParameters())
                        );
                    } catch (Exception e) {
                        errorLogger.warn(e, ErrorContext.builder().related("Registering PlaceholderAPI").build());
                        return null;
                    }
                }).build()
        );
    }

    private List<String> getPlaceholderParameterValues(PlaceholderParameters parameters) {
        return parameters.getAll().stream()
                .map(Parameter::getValue)
                .collect(Collectors.toList());
    }

    private PlayerContainer getPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        PlayerContainer container = system.getDatabaseSystem().getDatabase().query(ContainerFetchQueries.fetchPlayerContainer(uuid));
        SessionCache.getCachedSession(uuid).ifPresent(session -> container.putRawData(PlayerKeys.ACTIVE_SESSION, session));

        return container;
    }
}
