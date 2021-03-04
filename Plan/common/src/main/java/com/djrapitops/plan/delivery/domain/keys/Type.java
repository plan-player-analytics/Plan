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
package com.djrapitops.plan.delivery.domain.keys;

import java.util.Objects;

/**
 * Similar to Google's TypeToken but without requiring whole gson package.
 * <p>
 * Create new instance with {@code new Type<YourObject>() {}}.
 *
 * @author AuroraLS3
 */
public abstract class Type<T> {

    private final String genericsSuperClass;

    protected Type() {
        genericsSuperClass = getGenericsClass().getGenericSuperclass().getTypeName();
    }

    public static <K> Type<K> ofClass(Class<K> of) {
        return new Type<K>() {};
    }

    public static <K> Type<K> of(K object) {
        return new Type<K>() {};
    }

    public Class<Type<T>> getGenericsClass() {
        return (Class<Type<T>>) getClass();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Type)) return false;
        Type<?> type = (Type<?>) o;
        return Objects.equals(genericsSuperClass, type.genericsSuperClass);
    }

    @Override
    public int hashCode() {

        return Objects.hash(genericsSuperClass);
    }
}