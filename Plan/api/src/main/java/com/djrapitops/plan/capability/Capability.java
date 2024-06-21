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
package com.djrapitops.plan.capability;

import com.djrapitops.plan.delivery.web.ResourceService;

import java.util.Optional;
import java.util.UUID;

/**
 * List of different capabilities current version provides.
 * <p>
 * The enum is package private to restrict direct access. This is to avoid NoClassDefFoundError in case
 * a wanted Capability is not provided in an earlier version.
 * <p>
 * Use {@link CapabilityService#hasCapability(String)} with name of a Capability to figure out if an API is available.
 * Example usage: {@code CapabilityService.getInstance().hasCapability("DATA_EXTENSION_VALUES")}.
 * <p>
 * If a capability is not available, attempting to use the capability might lead to exceptions.
 *
 * @author AuroraLS3
 */
enum Capability {

    /**
     * ExtensionService, DataExtension API base package, PluginInfo, Conditional, Tab, TabInfo, TabOrder and BooleanProvider, DoubleProvider, PercentageProvider, NumberProvider, StringProvider annotations.
     */
    DATA_EXTENSION_VALUES,
    /**
     * DataExtension API table package, TableProvider, Table and Table.Factory
     */
    DATA_EXTENSION_TABLES,
    /**
     * DataExtension API groups, GroupProvider and Group parameter methods
     */
    DATA_EXTENSION_GROUPS,
    /**
     * DataExtension API addition, allows throwing {@link com.djrapitops.plan.extension.NotReadyException} inside a Provider method when your API is not ready for a method call.
     */
    DATA_EXTENSION_NOT_READY_EXCEPTION,
    /**
     * DataExtension API addition, parameter {@code showInPlayerTable} for BooleanProvider, DoubleProvider, PercentageProvider, NumberProvider, StringProvider annotations.
     * <p>
     * When the parameter is set to {@code true} the value from this Provider is shown on a table alongside players.
     */
    DATA_EXTENSION_SHOW_IN_PLAYER_TABLE,
    /**
     * DataExtension API addition, {@link com.djrapitops.plan.extension.builder.ExtensionDataBuilder}.
     */
    DATA_EXTENSION_BUILDER_API,
    /**
     * {@link com.djrapitops.plan.query.QueryService} and {@link com.djrapitops.plan.query.CommonQueries}
     */
    QUERY_API,
    /**
     * {@link com.djrapitops.plan.query.CommonQueries#fetchCurrentSessionPlaytime(UUID)}
     */
    QUERY_API_ACTIVE_SESSION_PLAYTIME,
    /**
     * {@link com.djrapitops.plan.settings.SettingsService}
     */
    SETTINGS_API,
    /**
     * {@link com.djrapitops.plan.delivery.web.ResolverService}
     */
    PAGE_EXTENSION_RESOLVERS,
    /**
     * {@link com.djrapitops.plan.delivery.web.ResolverService#getResolvers(String)}
     */
    PAGE_EXTENSION_RESOLVERS_LIST,
    /**
     * {@link com.djrapitops.plan.delivery.web.ResourceService}
     */
    PAGE_EXTENSION_RESOURCES,
    /**
     * {@link com.djrapitops.plan.delivery.web.ResourceService#addJavascriptToResource(String, String, ResourceService.Position, String, String)}
     * {@link com.djrapitops.plan.delivery.web.ResourceService#addStyleToResource(String, String, ResourceService.Position, String, String)}
     */
    PAGE_EXTENSION_RESOURCES_REGISTER_DIRECT_CUSTOMIZATION,
    /**
     * {@link  com.djrapitops.plan.delivery.web.ResolverService#registerPermissions(String...)}
     */
    PAGE_EXTENSION_USER_PERMISSIONS,
    /**
     * {@link com.djrapitops.plan.extension.annotation.GraphPointProvider}.
     */
    DATA_EXTENSION_GRAPH_API;

    static Optional<Capability> getByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}