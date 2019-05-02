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
import com.djrapitops.plan.extension.Group;
import com.djrapitops.plan.extension.implementation.MethodType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

/**
 * Wrap a Method so that it is easier to call.
 *
 * @author Rsl1122
 */
public class MethodWrapper<T> {

    private final Method method;
    private final Class<T> resultType;
    private MethodType methodType;

    public MethodWrapper(Method method, Class<T> resultType) {
        this.method = method;
        this.resultType = resultType;
        methodType = MethodType.forMethod(this.method);
    }

    public T callMethod(DataExtension extension, UUID playerUUID, String playerName) {
        if (methodType != MethodType.PLAYER_NAME && methodType != MethodType.PLAYER_UUID) {
            throw new IllegalStateException(method.getDeclaringClass() + " method " + method.getName() + " is not GROUP method.");
        }
        return callMethod(extension, playerUUID, playerName, null);
    }

    public T callMethod(DataExtension extension, Group group) {
        if (methodType != MethodType.GROUP) {
            throw new IllegalStateException(method.getDeclaringClass() + " method " + method.getName() + " is not GROUP method.");
        }
        return callMethod(extension, null, null, group);
    }

    public T callMethod(DataExtension extension) {
        if (methodType != MethodType.SERVER) {
            throw new IllegalStateException(method.getDeclaringClass() + " method " + method.getName() + " is not SERVER method.");
        }
        return callMethod(extension, null, null, null);
    }

    public T callMethod(DataExtension extension, UUID playerUUID, String playerName, Group group) {
        try {
            switch (methodType) {
                case SERVER:
                    return resultType.cast(method.invoke(extension));
                case PLAYER_UUID:
                    return resultType.cast(method.invoke(extension, playerUUID));
                case PLAYER_NAME:
                    return resultType.cast(method.invoke(extension, playerName));
                case GROUP:
                    return resultType.cast(method.invoke(extension, group));
                default:
                    throw new IllegalArgumentException(method.getDeclaringClass() + " method " + method.getName() + " had invalid parameters.");
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            Throwable cause = e.getCause();
            boolean hasCause = cause != null;
            throw new IllegalArgumentException(method.getDeclaringClass() + " method " + method.getName() + " had invalid parameters; caused " +
                    (hasCause ? cause.toString() : e.toString()));
        }
    }

    public String getMethodName() {
        return method.getName();
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public Class<T> getResultType() {
        return resultType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodWrapper)) return false;
        MethodWrapper<?> that = (MethodWrapper<?>) o;
        return method.equals(that.method) &&
                resultType.equals(that.resultType) &&
                methodType == that.methodType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, resultType, methodType);
    }
}