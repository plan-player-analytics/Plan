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
package com.djrapitops.plan.extension.implementation.builder;

import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.implementation.ProviderInformation;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class BuiltDataValue<T> implements DataValue<T> {

    private final T value;
    private final Supplier<T> supplier;
    private final ProviderInformation information;

    public BuiltDataValue(T value, ProviderInformation information) {
        this(value, null, information);
    }

    public BuiltDataValue(Supplier<T> supplier, ProviderInformation information) {
        this(null, supplier, information);
    }

    private BuiltDataValue(T value, Supplier<T> supplier, ProviderInformation information) {
        this.value = value;
        this.supplier = supplier;
        this.information = information;
    }

    @Override
    public T getValue() {
        if (value != null) return value;
        if (supplier != null) return supplier.get();
        return null;
    }

    public ProviderInformation getInformation() {
        return information;
    }

    @Override
    public <M> M getInformation(Class<M> ofType) {
        if (ProviderInformation.class.equals(ofType)) return ofType.cast(getInformation());
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuiltDataValue<?> that = (BuiltDataValue<?>) o;
        return Objects.equals(value, that.value) && Objects.equals(supplier, that.supplier) && Objects.equals(information, that.information);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, supplier, information);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "value=" + value +
                ", supplier=" + supplier +
                ", information=" + information +
                '}';
    }
}
