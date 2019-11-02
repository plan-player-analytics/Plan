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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Group class for handling multiple different types of {@link DataProvider}s.
 *
 * @author Rsl1122
 */
public class DataProviders {

    private final Map<MethodType, Map<Class, List<DataProvider>>> byMethodType;

    public DataProviders() {
        byMethodType = new EnumMap<>(MethodType.class);
    }

    public <T> void put(DataProvider<T> provider) {
        MethodWrapper<T> method = provider.getMethod();

        MethodType methodType = method.getMethodType();
        Class<T> resultType = method.getResultType();

        Map<Class, List<DataProvider>> byParameterType = byMethodType.getOrDefault(methodType, new HashMap<>());
        List<DataProvider> dataProviders = byParameterType.getOrDefault(resultType, new ArrayList<>());

        dataProviders.add(provider);

        byParameterType.put(resultType, dataProviders);
        byMethodType.put(methodType, byParameterType);
    }

    public <T> List<DataProvider<T>> getPlayerMethodsByType(Class<T> returnType) {
        Map<Class, List<DataProvider>> providersAcceptingUUID = byMethodType.getOrDefault(MethodType.PLAYER_UUID, new HashMap<>());
        Map<Class, List<DataProvider>> providersAcceptingName = byMethodType.getOrDefault(MethodType.PLAYER_NAME, new HashMap<>());

        List<DataProvider<T>> byReturnType = new ArrayList<>();
        for (DataProvider dataProvider : providersAcceptingUUID.getOrDefault(returnType, Collections.emptyList())) {
            byReturnType.add((DataProvider<T>) dataProvider);
        }
        for (DataProvider dataProvider : providersAcceptingName.getOrDefault(returnType, Collections.emptyList())) {
            byReturnType.add((DataProvider<T>) dataProvider);
        }
        return byReturnType;
    }

    public <T> List<DataProvider<T>> getServerMethodsByType(Class<T> returnType) {
        List<DataProvider<T>> byReturnType = new ArrayList<>();
        for (DataProvider dataProvider : byMethodType.getOrDefault(MethodType.SERVER, new HashMap<>()).getOrDefault(returnType, Collections.emptyList())) {
            byReturnType.add((DataProvider<T>) dataProvider);
        }
        return byReturnType;
    }

    public <T> List<DataProvider<T>> getGroupMethodsByType(Class<T> returnType) {
        List<DataProvider<T>> byReturnType = new ArrayList<>();
        for (DataProvider dataProvider : byMethodType.getOrDefault(MethodType.GROUP, new HashMap<>()).getOrDefault(returnType, Collections.emptyList())) {
            byReturnType.add((DataProvider<T>) dataProvider);
        }
        return byReturnType;
    }

    public void removeProviderWithMethod(MethodWrapper toRemove) {
        MethodType methodType = toRemove.getMethodType();
        Map<Class, List<DataProvider>> byResultType = byMethodType.getOrDefault(methodType, Collections.emptyMap());
        if (byResultType.isEmpty()) {
            return;
        }

        Class resultType = toRemove.getResultType();
        List<DataProvider> providers = byResultType.getOrDefault(resultType, Collections.emptyList());
        if (providers.isEmpty()) {
            return;
        }

        byResultType.put(resultType, providers.stream()
                .filter(provider -> !provider.getMethod().equals(toRemove))
                .collect(Collectors.toList())
        );
        byMethodType.put(methodType, byResultType);
    }
}