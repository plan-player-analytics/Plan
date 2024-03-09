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

import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.builder.ValueBuilder;

/**
 * Interface to implement data extensions with.
 * <p>
 * The class implementing this interface should be annotated with {@link com.djrapitops.plan.extension.annotation.PluginInfo}.
 * If the extension is given to Plan API without the annotation it will be rejected.
 * <hr>
 * <p>
 * Public methods in the class should be annotated with appropriate Provider annotations.
 * Provider annotations:
 * {@link com.djrapitops.plan.extension.annotation.BooleanProvider} for {@code boolean} values and conditions for {@link com.djrapitops.plan.extension.annotation.Conditional}.
 * {@link com.djrapitops.plan.extension.annotation.NumberProvider} for {@code long} values. (Use this for integers by casting to long) Has option for formatting.
 * {@link com.djrapitops.plan.extension.annotation.DoubleProvider} for {@code double} values.
 * {@link com.djrapitops.plan.extension.annotation.PercentageProvider} for {@code double} values that represent a percentage.
 * {@link com.djrapitops.plan.extension.annotation.StringProvider} for {@link String} values.
 * {@link com.djrapitops.plan.extension.annotation.TableProvider} for {@link com.djrapitops.plan.extension.table.Table}s.
 * {@link com.djrapitops.plan.extension.annotation.GroupProvider} for Player specific group names, such as permission groups.
 * {@link com.djrapitops.plan.extension.annotation.DataBuilderProvider} for {@link ExtensionDataBuilder}s.
 * <hr>
 * <p>
 * Methods can have one of the following as method parameters:
 * {@code UUID playerUUID} - UUID of the player the data is about
 * {@code String playerName} - Name of the player the data is about
 * {@link Group group} - Provided group the data is about (In case a group needs additional information)
 * nothing - The data is interpreted to be about the server.
 * <hr>
 * <p>
 * The name of the method will be used as an identifier in the database, so that a single provider does not duplicate entries.
 * Only first 50 characters of the method name are stored.
 * If you need to change a method name add a class annotation with the old method name: {@link com.djrapitops.plan.extension.annotation.InvalidateMethod}
 * <p>
 * Some additional annotations are available for controlling appearance of the results:
 * {@link com.djrapitops.plan.extension.annotation.Conditional} A {@code boolean} returned by {@link com.djrapitops.plan.extension.annotation.BooleanProvider} has to be {@code true} for this method to be called.
 * {@link com.djrapitops.plan.extension.annotation.Tab} The value of this provider should be placed on a tab with a specific name
 * {@link com.djrapitops.plan.extension.annotation.TabInfo} Optional Structure information about a tab
 * {@link com.djrapitops.plan.extension.annotation.TabOrder} Optional information about preferred tab
 * <hr>
 * <p>
 * Method calls are asynchronous. You can control when the calls are made via {@link DataExtension#callExtensionMethodsOn()} and {@link Caller}.
 * <p>
 * You can check against implementation violations by using {@link com.djrapitops.plan.extension.extractor.ExtensionExtractor#validateAnnotations()} in your Unit Tests.
 * <p>
 * Implementation violations:
 * - No {@link com.djrapitops.plan.extension.annotation.PluginInfo} class annotation
 * - Class contains no public methods with Provider annotations
 * - Class contains private method with Provider annotation
 * - Non-primitive return type when primitive is required (eg. Boolean instead of boolean)
 * - Method doesn't have correct parameters (see above)
 * - {@link com.djrapitops.plan.extension.annotation.BooleanProvider} is annotated with a {@link com.djrapitops.plan.extension.annotation.Conditional} that requires same condition the provider provides.
 * - {@link com.djrapitops.plan.extension.annotation.Conditional} without a {@link com.djrapitops.plan.extension.annotation.BooleanProvider} that provides value for the condition
 * - Annotation variable is over 50 characters (Or 150 if description)
 * - Method name is over 50 characters (Used as an identifier for storage)
 *
 * @author AuroraLS3
 * @see com.djrapitops.plan.extension.annotation.PluginInfo Required Annotation
 * @see CallEvents for method call event types.
 */
public interface DataExtension {

    /**
     * Determines when DataExtension methods are called automatically by Plan.
     * <p>
     * Override this method to determine more suitable call times for your plugin.
     * You can also use {@link Caller} to update manually.
     * <p>
     * If an empty array is supplied the DataExtension methods are not called by Plan automatically.
     *
     * @return Event types that will trigger method calls to the DataExtension.
     * @see CallEvents for details when the methods are called.
     */
    default CallEvents[] callExtensionMethodsOn() {
        return new CallEvents[]{
                CallEvents.PLAYER_JOIN,
                CallEvents.PLAYER_LEAVE,
                CallEvents.SERVER_EXTENSION_REGISTER
        };
    }

    /**
     * Obtain a new {@link ExtensionDataBuilder}.
     * <p>
     * Requires Capability DATA_EXTENSION_BUILDER_API
     *
     * @return new builder.
     */
    default ExtensionDataBuilder newExtensionDataBuilder() {
        return ExtensionService.getInstance().newExtensionDataBuilder(this);
    }

    /**
     * Obtain a new {@link ValueBuilder} to use with {@link ExtensionDataBuilder}.
     * <p>
     * Requires Capability DATA_EXTENSION_BUILDER_API
     *
     * @param text Text that is displayed next to the value.
     * @return new builder.
     */
    default ValueBuilder valueBuilder(String text) {
        return newExtensionDataBuilder().valueBuilder(text);
    }

    /**
     * Get the name of the plugin from PluginInfo annotation.
     * <p>
     * Requires Capability DATA_EXTENSION_BUILDER_API
     *
     * @return new builder.
     */
    default String getPluginName() {
        PluginInfo annotation = getClass().getAnnotation(PluginInfo.class);
        if (annotation == null) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " did not have @PluginInfo annotation!");
        }
        return annotation.name();
    }

}
