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
package com.djrapitops.plan.extension;

/**
 * Interface to implement data extensions with.
 * <p>
 * The class implementing this interface should be annotated with {@link com.djrapitops.plan.extension.annotation.PluginInfo}.
 * If the extension is given to Plan API without the annotation it will be rejected.
 * <p>
 * Public methods in the class should be annotated with appropriate Provider annotations.
 * Provider annotations:
 * {@link com.djrapitops.plan.extension.annotation.BooleanProvider} for {@code boolean} values and conditions for {@link com.djrapitops.plan.extension.annotation.Conditional}.
 * {@link com.djrapitops.plan.extension.annotation.NumberProvider} for {@code long} values. (Use this for integers by casting to long) Has option for formatting.
 * {@link com.djrapitops.plan.extension.annotation.DoubleProvider} for {@code double} values.
 * {@link com.djrapitops.plan.extension.annotation.PercentageProvider} for {@code double} values that represent a percentage.
 * {@link com.djrapitops.plan.extension.annotation.StringProvider} for {@link String} values.
 * <p>
 * Methods can have one of the following as method parameters:
 * {@code UUID playerUUID} - UUID of the player the data is about
 * {@code String playerName} - Name of the player the data is about
 * {@link Group group} - Provided group the data is about (In case a group needs additional information)
 * nothing - The data is interpreted to be about the server.
 * <p>
 * The name of the method will be used as an identifier in the database, so that a single provider does not duplicate entries.
 * Only first 50 characters of the method name are stored.
 * If you need to change a method name add a class annotation with the old method name: {@link com.djrapitops.plan.extension.annotation.InvalidateMethod}
 * <p>
 * Some additional annotations are available for controlling appearance of the results:
 * {@link com.djrapitops.plan.extension.annotation.Conditional} A {@code boolean} returned by {@link com.djrapitops.plan.extension.annotation.BooleanProvider} has to be {@code true} for this method to be called.
 * {@link com.djrapitops.plan.extension.annotation.Tab} The value of this provider should be placed on a tab with a specific name
 * {@link com.djrapitops.plan.extension.annotation.TabInfo} Optional Structure information about a tab
 * {@link com.djrapitops.plan.extension.annotation.TabOrder} Optional information about preferred tab order
 *
 * @author Rsl1122
 */
public interface DataExtension {
}
