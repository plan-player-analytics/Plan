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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Group class for handling multiple different types of {@link DataProvider}s.
 *
 * @author Rsl1122
 */
public class DataProviders {

    private Map<Class, Map<Class, List<DataProvider>>> byReturnType;

    public DataProviders() {
        byReturnType = new HashMap<>();
    }

    public <T, K> void put(Class<T> parameterType, Class<K> returnType, DataProvider<T, K> provider) {
        Map<Class, List<DataProvider>> byParameterType = byReturnType.getOrDefault(returnType, new HashMap<>());
        List<DataProvider> dataProviders = byParameterType.getOrDefault(parameterType, new ArrayList<>());

        dataProviders.add(provider);

        byParameterType.put(parameterType, dataProviders);
        byReturnType.put(returnType, byParameterType);
    }

    public <T, K> List<DataProvider> get(Class<T> parameterType, Class<K> returnType) {
        return byReturnType.getOrDefault(returnType, new HashMap<>()).getOrDefault(parameterType, new ArrayList<>());
    }
}