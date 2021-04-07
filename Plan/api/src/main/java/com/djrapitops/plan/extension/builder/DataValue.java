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
package com.djrapitops.plan.extension.builder;

import java.util.Optional;

/**
 * Represents a value given to {@link ExtensionDataBuilder}.
 * <p>
 * Please do not implement this class, it is an implementation detail.
 * Obtain instances with {@link ValueBuilder}.
 *
 * @param <T> Type of the value.
 */
public interface DataValue<T> {

    T getValue();

    <M> M getInformation(Class<M> ofType);

    default <I extends DataValue<T>> Optional<I> getMetadata(Class<I> metadataType) {
        if (getClass().equals(metadataType)) return Optional.of(metadataType.cast(this));
        return Optional.empty();
    }

}
