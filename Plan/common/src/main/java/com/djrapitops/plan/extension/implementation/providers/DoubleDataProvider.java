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

import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.annotation.DoubleProvider;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ProviderInformation;

import java.lang.reflect.Method;

/**
 * Contains code that acts on {@link DoubleProvider} annotations.
 *
 * @author AuroraLS3
 */
public class DoubleDataProvider {

    private DoubleDataProvider() {
        // Static method class
    }

    public static void placeToDataProviders(
            DataProviders dataProviders, Method method, DoubleProvider annotation,
            Conditional condition, String tab, String pluginName
    ) {
        ProviderInformation information = ProviderInformation.builder(pluginName)
                .setName(method.getName())
                .setText(annotation.text())
                .setDescription(annotation.description())
                .setPriority(annotation.priority())
                .setIcon(new Icon(
                        annotation.iconFamily(),
                        annotation.iconName(),
                        annotation.iconColor())
                ).setShowInPlayersTable(annotation.showInPlayerTable())
                .setCondition(condition)
                .setTab(tab)
                .build();

        MethodWrapper<Double> methodWrapper = new MethodWrapper<>(method, Double.class);

        dataProviders.put(new DataProvider<>(information, methodWrapper));
    }
}