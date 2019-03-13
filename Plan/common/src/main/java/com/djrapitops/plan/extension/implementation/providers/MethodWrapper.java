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

import com.djrapitops.plan.extension.DataExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrap a Method so that it is easier to call.
 *
 * @author Rsl1122
 */
public class MethodWrapper<T, K> {

    private final Method method;
    private final Class<K> result;

    public MethodWrapper(Method method, Class<T> parameter, Class<K> result) {
        this.method = method;
        this.result = result;
    }

    public K callMethod(DataExtension extension, T parameter) throws InvocationTargetException, IllegalAccessException {
        return result.cast(method.invoke(extension, parameter));
    }

    public String getMethodName() {
        return method.getName();
    }
}