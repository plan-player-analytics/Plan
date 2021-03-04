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

import com.djrapitops.plan.extension.implementation.ProviderInformation;

import java.util.Objects;

/**
 * Representation of all values a Provider annotation provides.
 *
 * @author AuroraLS3
 */
public class DataProvider<T> {

    private final ProviderInformation providerInformation;
    private final MethodWrapper<T> method;

    public DataProvider(ProviderInformation providerInformation, MethodWrapper<T> method) {
        this.providerInformation = providerInformation;
        this.method = method;
    }

    public MethodWrapper<T> getMethod() {
        return method;
    }

    public ProviderInformation getProviderInformation() {
        return providerInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataProvider)) return false;
        DataProvider<?> that = (DataProvider<?>) o;
        return Objects.equals(providerInformation, that.providerInformation) &&
                Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerInformation, method);
    }
}