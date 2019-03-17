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

import com.djrapitops.plan.extension.annotation.BooleanProvider;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ProviderInformation;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Represents a DataExtension API method annotated with {@link BooleanProvider} annotation.
 * <p>
 * Used to obtain data to place in the database.
 *
 * @author Rsl1122
 */
public class BooleanDataProvider extends DataProvider<Boolean> {

    private final String providedCondition;

    private BooleanDataProvider(ProviderInformation providerInformation, MethodWrapper<Boolean> method, String providedCondition) {
        super(providerInformation, method);

        this.providedCondition = providedCondition;
    }

    public static void placeToDataProviders(
            DataProviders dataProviders, Method method, BooleanProvider annotation,
            String condition, String tab, String pluginName
    ) {
        MethodWrapper<Boolean> methodWrapper = new MethodWrapper<>(method, Boolean.class);
        Icon providerIcon = new Icon(annotation.iconFamily(), annotation.iconName(), annotation.iconColor());

        ProviderInformation providerInformation = new ProviderInformation(
                pluginName, method.getName(), annotation.text(), annotation.description(), providerIcon, annotation.priority(), tab, condition
        );

        dataProviders.put(new BooleanDataProvider(providerInformation, methodWrapper, annotation.conditionName()));
    }

    public Optional<String> getProvidedCondition() {
        return Optional.ofNullable(providedCondition);
    }
}