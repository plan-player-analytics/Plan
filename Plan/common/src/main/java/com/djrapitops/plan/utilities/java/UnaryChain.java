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
package com.djrapitops.plan.utilities.java;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Utility for combining multiple UnaryOperator modifications.
 *
 * @author AuroraLS3
 */
public class UnaryChain<T> {

    private final T modifying;
    private Function<T, T> modifier;

    private UnaryChain(T modifying) {
        this.modifying = modifying;
    }

    public static <V> UnaryChain<V> of(V modifying) {
        return new UnaryChain<>(modifying);
    }

    public UnaryChain<T> chain(UnaryOperator<T> operation) {
        if (modifier == null) {
            modifier = operation;
        } else {
            modifier = modifier.andThen(operation);
        }
        return this;
    }

    public T apply() {
        return modifier.apply(modifying);
    }
}
