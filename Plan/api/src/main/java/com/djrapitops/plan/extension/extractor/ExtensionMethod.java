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
package com.djrapitops.plan.extension.extractor;

import com.djrapitops.plan.extension.Group;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.UUID;

public class ExtensionMethod {

    private final Class<?> returnType;
    private final Method method;

    public ExtensionMethod(Method method) {
        this.method = method;
        returnType = method.getReturnType();
    }

    public boolean isInaccessible() {
        int modifiers = method.getModifiers();
        return Modifier.isPrivate(modifiers)
                || Modifier.isProtected(modifiers)
                || Modifier.isStatic(modifiers)
                || Modifier.isNative(modifiers);
    }

    public <T extends Annotation> Optional<T> getAnnotation(Class<T> ofType) {
        return Optional.ofNullable(method.getAnnotation(ofType));
    }

    public ParameterType getParameterType() {
        return ParameterType.getByMethodSignature(method);
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Method getMethod() {
        return method;
    }

    public enum ParameterType {
        SERVER_NONE(null),
        PLAYER_STRING(String.class),
        PLAYER_UUID(UUID.class),
        GROUP(Group.class);

        private final Class<?> parameterType;

        ParameterType(Class<?> parameterType) {
            this.parameterType = parameterType;
        }

        public static ParameterType getByMethodSignature(Method method) {
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length == 0) return SERVER_NONE;

            if (parameters.length > 1) {
                // Has too many parameters
                throw new IllegalArgumentException(method.getName() + " has too many parameters, only one parameter is required.");
            }

            Class<?> parameter = parameters[0];
            if (String.class.equals(parameter)) return PLAYER_STRING;
            if (UUID.class.equals(parameter)) return PLAYER_UUID;
            if (Group.class.equals(parameter)) return GROUP;

            throw new IllegalArgumentException(method.getName() + " does not have a valid parameter. Needs none, String, UUID or Group, had " + parameter.getSimpleName());
        }

        public Class<?> getParameterType() {
            return parameterType;
        }
    }

}
