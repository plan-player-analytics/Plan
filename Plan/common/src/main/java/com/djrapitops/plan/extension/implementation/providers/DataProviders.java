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

import com.djrapitops.plan.extension.implementation.MethodType;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.java.Maps;

import java.util.*;

/**
 * Group class for handling multiple different types of {@link DataProvider}s.
 *
 * @author Rsl1122
 */
public class DataProviders {

    private final Map<MethodType, Map<Class<?>, List<DataProvider<?>>>> byMethodType;

    public DataProviders() {
        byMethodType = new EnumMap<>(MethodType.class);
    }

    public void put(DataProvider<?> provider) {
        MethodWrapper<?> method = provider.getMethod();
        MethodType methodType = method.getMethodType();
        Class<?> returnType = method.getReturnType();

        computeIfAbsent(methodType, returnType).add(provider);
    }

    private List<DataProvider<?>> computeIfAbsent(MethodType methodType, Class<?> returnType) {
        return byMethodType.computeIfAbsent(methodType, Maps::create).computeIfAbsent(returnType, Lists::create);
    }

    private List<DataProvider<?>> getProvidersByTypes(MethodType methodType, Class<?> returnType) {
        Map<Class<?>, List<DataProvider<?>>> byReturnType = byMethodType.getOrDefault(methodType, Collections.emptyMap());
        return byReturnType.getOrDefault(returnType, Collections.emptyList());
    }

    public <T> List<DataProvider<T>> getPlayerMethodsByType(Class<T> returnType) {
        List<DataProvider<T>> byReturnType = new ArrayList<>();
        for (DataProvider<?> dataProvider : getProvidersByTypes(MethodType.PLAYER_UUID, returnType)) {
            byReturnType.add((DataProvider<T>) dataProvider);
        }
        for (DataProvider<?> dataProvider : getProvidersByTypes(MethodType.PLAYER_NAME, returnType)) {
            byReturnType.add((DataProvider<T>) dataProvider);
        }
        return byReturnType;
    }

    public <T> List<DataProvider<T>> getServerMethodsByType(Class<T> returnType) {
        List<DataProvider<T>> providers = new ArrayList<>();
        for (DataProvider<?> dataProvider : getProvidersByTypes(MethodType.SERVER, returnType)) {
            providers.add((DataProvider<T>) dataProvider);
        }
        return providers;
    }

    public <T> List<DataProvider<T>> getGroupMethodsByType(Class<T> returnType) {
        List<DataProvider<T>> byReturnType = new ArrayList<>();
        for (DataProvider<?> dataProvider : getProvidersByTypes(MethodType.GROUP, returnType)) {
            byReturnType.add((DataProvider<T>) dataProvider);
        }
        return byReturnType;
    }

    public void removeProviderWithMethod(MethodWrapper<?> toRemove) {
        MethodType methodType = toRemove.getMethodType();
        Map<Class<?>, List<DataProvider<?>>> byResultType = byMethodType.getOrDefault(methodType, Collections.emptyMap());
        if (byResultType.isEmpty()) {
            return;
        }

        Class<?> returnType = toRemove.getReturnType();
        List<DataProvider<?>> providers = getProvidersByTypes(methodType, returnType);

        DataProvider<?> providerToRemove = null;
        for (DataProvider<?> provider : providers) {
            if (provider.getMethod().equals(toRemove)) {
                providerToRemove = provider;
                break;
            }
        }
        providers.remove(providerToRemove);
    }
}