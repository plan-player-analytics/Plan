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

import com.djrapitops.plan.exceptions.DataExtensionMethodCallException;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.implementation.MethodType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * Wrap a Method so that it is easier to call.
 *
 * @author AuroraLS3
 */
public class MethodWrapper<T> {

    private final Method method;
    private final Class<T> returnType;
    private final MethodType methodType;
    private boolean disabled = false;

    public MethodWrapper(Method method, Class<T> returnType) {
        this.method = method;
        this.returnType = returnType;
        methodType = MethodType.forMethod(this.method);
    }

    public T callMethod(DataExtension extension, Parameters with) {
        if (disabled) return null;
        try {
            return returnType.cast(with.usingOn(extension, method));
        } catch (InvocationTargetException notReadyToBeCalled) {
            Throwable cause = notReadyToBeCalled.getCause();
            if (cause instanceof NotReadyException || cause instanceof UnsupportedOperationException) {
                return null; // Data or API not available to make the call.
            } else {
                throw new DataExtensionMethodCallException(getErrorMessage(extension, notReadyToBeCalled), notReadyToBeCalled, extension.getPluginName(), getMethodName());
            }
        } catch (IllegalAccessException e) {
            throw new DataExtensionMethodCallException(extension.getPluginName() + '.' + getMethodName() + " could not be accessed: " + e.getMessage(), e, extension.getPluginName(), getMethodName());
        }
    }

    private String getErrorMessage(DataExtension extension, InvocationTargetException e) {
        Optional<Throwable> actualCause = Optional.ofNullable(e.getCause()); // InvocationTargetException
        return extension.getPluginName() + '.' + getMethodName() + " errored: " + actualCause.orElse(e).toString();
    }

    public String getMethodName() {
        return method.getName();
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public Class<T> getReturnType() {
        return returnType;
    }

    public void disable() {
        this.disabled = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodWrapper)) return false;
        MethodWrapper<?> that = (MethodWrapper<?>) o;
        return method.equals(that.method) &&
                returnType.equals(that.returnType) &&
                methodType == that.methodType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, returnType, methodType);
    }
}