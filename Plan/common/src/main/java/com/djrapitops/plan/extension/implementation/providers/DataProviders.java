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
 * @author AuroraLS3
 */
@Deprecated
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

    public <T> List<DataProvider<T>> getProvidersByTypes(MethodType methodType, Class<T> returnType) {
        Map<Class<?>, List<DataProvider<?>>> byReturnType = byMethodType.getOrDefault(methodType, Collections.emptyMap());
        List<DataProvider<?>> wildcardProvidersWithSpecificType = byReturnType.get(returnType);
        if (wildcardProvidersWithSpecificType == null) {
            return Collections.emptyList();
        }
        // Cast to T
        List<DataProvider<T>> providers = new ArrayList<>();
        for (DataProvider<?> dataProvider : wildcardProvidersWithSpecificType) {
            providers.add((DataProvider<T>) dataProvider);
        }
        return providers;
    }

    public <T> List<DataProvider<T>> getPlayerMethodsByType(Class<T> returnType) {
        return getProvidersByTypes(MethodType.PLAYER, returnType);
    }

    public <T> List<DataProvider<T>> getServerMethodsByType(Class<T> returnType) {
        return getProvidersByTypes(MethodType.SERVER, returnType);
    }

    public <T> List<DataProvider<T>> getGroupMethodsByType(Class<T> returnType) {
        return getProvidersByTypes(MethodType.GROUP, returnType);
    }

    public <T> void removeProviderWithMethod(MethodWrapper<T> toRemove) {
        MethodType methodType = toRemove.getMethodType();
        Map<Class<?>, List<DataProvider<?>>> byResultType = byMethodType.getOrDefault(methodType, Collections.emptyMap());
        if (byResultType.isEmpty()) {
            return;
        }

        Class<T> returnType = toRemove.getReturnType();
        List<DataProvider<T>> providers = getProvidersByTypes(methodType, returnType);

        DataProvider<T> providerToRemove = null;
        for (DataProvider<T> provider : providers) {
            if (provider.getMethod().equals(toRemove)) {
                providerToRemove = provider;
                break;
            }
        }
        providers.remove(providerToRemove);
    }
}