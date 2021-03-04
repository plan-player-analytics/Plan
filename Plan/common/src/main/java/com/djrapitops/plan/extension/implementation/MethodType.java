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
package com.djrapitops.plan.extension.implementation;

import com.djrapitops.plan.extension.Group;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Utility enum for determining what kind of parameters a provider method used.
 * <p>
 * This also allows figuring out where to save method results.
 *
 * @author AuroraLS3
 */
public enum MethodType {

    PLAYER,
    GROUP,
    SERVER;

    public static MethodType forMethod(Method method) {
        int parameterCount = method.getParameterCount();
        if (parameterCount == 0) {
            return SERVER;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> firstParameter = parameterTypes[0];

        if (UUID.class.equals(firstParameter) || String.class.equals(firstParameter)) {
            return PLAYER;
        } else if (Group.class.equals(firstParameter)) {
            return GROUP;
        }

        throw new IllegalArgumentException(method.getDeclaringClass() + " method " + method.getName() + " had invalid parameters.");
    }

}