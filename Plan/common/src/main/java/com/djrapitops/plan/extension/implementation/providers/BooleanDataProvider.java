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
import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import org.apache.commons.lang3.StringUtils;

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
    private final boolean hidden;

    private BooleanDataProvider(ProviderInformation providerInformation, MethodWrapper<Boolean> method, String providedCondition, boolean hidden) {
        super(providerInformation, method);

        this.providedCondition = providedCondition;
        this.hidden = hidden;
    }

    public static void placeToDataProviders(
            DataProviders dataProviders, Method method, BooleanProvider annotation,
            Conditional condition, String tab, String pluginName
    ) {
        MethodWrapper<Boolean> methodWrapper = new MethodWrapper<>(method, Boolean.class);
        Icon providerIcon = new Icon(annotation.iconFamily(), annotation.iconName(), annotation.iconColor());

        ProviderInformation providerInformation = new ProviderInformation(
                pluginName, method.getName(), annotation.text(), annotation.description(), providerIcon, annotation.priority(), annotation.showInPlayerTable(), tab, condition
        );

        dataProviders.put(new BooleanDataProvider(providerInformation, methodWrapper, annotation.conditionName(), annotation.hidden()));
    }

    public static Optional<String> getProvidedCondition(DataProvider<?> provider) {
        if (provider instanceof BooleanDataProvider) {
            return ((BooleanDataProvider) provider).getProvidedCondition();
        }
        return Optional.empty();
    }

    public Optional<String> getProvidedCondition() {
        return providedCondition == null || providedCondition.isEmpty() ? Optional.empty() : Optional.of(StringUtils.truncate(providedCondition, 50));
    }

    public static boolean isHidden(DataProvider<?> provider) {
        return provider instanceof BooleanDataProvider && ((BooleanDataProvider) provider).isHidden();
    }

    public boolean isHidden() {
        return hidden;
    }
}