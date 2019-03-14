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
package com.djrapitops.plan.extension.implementation.providers;

import com.djrapitops.plan.extension.Group;
import com.djrapitops.plan.extension.annotation.DoubleProvider;
import com.djrapitops.plan.extension.icon.Icon;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a DataExtension API method annotated with {@link DoubleProvider} annotation.
 * <p>
 * Used to obtain data to place in the database.
 *
 * @author Rsl1122
 */
public class DoubleDataProvider<T> extends DataProvider<T, Double> {

    private DoubleDataProvider(
            String plugin, String condition, String tab, int priority, Icon icon,
            String text, String description, MethodWrapper<T, Double> method
    ) {
        super(plugin, condition, tab, priority, icon, text, description, method);
    }

    public static void placeToDataProviders(
            DataProviders dataProviders, Method method, DoubleProvider annotation,
            String condition, String tab, String pluginName, Optional<Class> parameterClass
    ) {
        if (parameterClass.isPresent()) {
            if (UUID.class.isAssignableFrom(parameterClass.get())) {
                dataProviders.put(
                        UUID.class, Double.class,
                        new DoubleDataProvider<>(
                                pluginName, condition, tab,
                                annotation.priority(),
                                new Icon(annotation.iconFamily(), annotation.iconName(), annotation.iconColor()),
                                annotation.text(), annotation.description(),
                                new MethodWrapper<>(method, UUID.class, Double.class)
                        )
                );
            } else if (String.class.isAssignableFrom(parameterClass.get())) {
                dataProviders.put(
                        String.class, Double.class,
                        new DoubleDataProvider<>(
                                pluginName, condition, tab,
                                annotation.priority(),
                                new Icon(annotation.iconFamily(), annotation.iconName(), annotation.iconColor()),
                                annotation.text(), annotation.description(),
                                new MethodWrapper<>(method, String.class, Double.class)
                        )
                );
            } else if (Group.class.isAssignableFrom(parameterClass.get())) {
                dataProviders.put(
                        Group.class, Double.class,
                        new DoubleDataProvider<>(
                                pluginName, condition, tab,
                                annotation.priority(),
                                new Icon(annotation.iconFamily(), annotation.iconName(), annotation.iconColor()),
                                annotation.text(), annotation.description(),
                                new MethodWrapper<>(method, Group.class, Double.class)
                        )
                );
            }
        } else {
            dataProviders.put(
                    null, Double.class,
                    new DoubleDataProvider<>(
                            pluginName, condition, tab,
                            annotation.priority(),
                            new Icon(annotation.iconFamily(), annotation.iconName(), annotation.iconColor()),
                            annotation.text(), annotation.description(),
                            new MethodWrapper<>(method, null, Double.class)
                    )
            );
        }
    }
}