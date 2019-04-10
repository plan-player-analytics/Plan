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

import java.util.Optional;

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
 * @author Rsl1122
 */
enum Capability {

    /**
     * ExtensionService, DataExtension API base package, PluginInfo, Conditional, Tab, TabInfo, TabOrder and BooleanProvider, DoubleProvider, PercentageProvider, NumberProvider, StringProvider annotations.
     */
    DATA_EXTENSION_VALUES,
    /**
     * DataExtension API table package, TableProvider, Table and Table.Factory
     */
    DATA_EXTENSION_TABLES;

    static Optional<Capability> getByName(String name) {
        try {
            return Optional.of(valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}