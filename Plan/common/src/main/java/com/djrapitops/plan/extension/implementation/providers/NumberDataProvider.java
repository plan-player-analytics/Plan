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
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.icon.Icon;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a DataExtension API method annotated with {@link NumberProvider} annotation.
 * <p>
 * Used to obtain data to place in the database.
 *
 * @author Rsl1122
 */
public class NumberDataProvider<T> extends DataProvider<T, Long> {

    private NumberDataProvider(
            String plugin, String condition, String tab, int priority, Icon icon,
            String text, String description, MethodWrapper<T, Long> method
    ) {
        super(plugin, condition, tab, priority, icon, text, description, method);
    }

    public static void placeToDataProviders(
            DataProviders dataProviders, Method method, NumberProvider annotation,
            String condition, String tab, String pluginName, Optional<Class> parameterClass
    ) {
        if (parameterClass.isPresent()) {
            if (UUID.class.isAssignableFrom(parameterClass.get())) {
                dataProviders.put(
                        UUID.class, Long.class,
                        new NumberDataProvider<>(
                                pluginName, condition, tab,
                                annotation.priority(),
                                new Icon(annotation.iconFamily(), annotation.iconName(), annotation.iconColor()),
                                annotation.text(), annotation.description(),
                                new MethodWrapper<>(method, UUID.class, Long.class)
                        )
                );
            } else if (String.class.isAssignableFrom(parameterClass.get())) {
                dataProviders.put(
                        String.class, Long.class,
                        new NumberDataProvider<>(
                                pluginName, condition, tab,
                                annotation.priority(),
                                new Icon(annotation.iconFamily(), annotation.iconName(), annotation.iconColor()),
                                annotation.text(), annotation.description(),
                                new MethodWrapper<>(method, String.class, Long.class)
                        )
                );
            } else if (Group.class.isAssignableFrom(parameterClass.get())) {
                dataProviders.put(
                        Group.class, Long.class,
                        new NumberDataProvider<>(
                                pluginName, condition, tab,
                                annotation.priority(),
                                new Icon(annotation.iconFamily(), annotation.iconName(), annotation.iconColor()),
                                annotation.text(), annotation.description(),
                                new MethodWrapper<>(method, Group.class, Long.class)
                        )
                );
            }
        } else {
            dataProviders.put(
                    null, Long.class,
                    new NumberDataProvider<>(
                            pluginName, condition, tab,
                            annotation.priority(),
                            new Icon(annotation.iconFamily(), annotation.iconName(), annotation.iconColor()),
                            annotation.text(), annotation.description(),
                            new MethodWrapper<>(method, null, Long.class)
                    )
            );
        }
    }
}