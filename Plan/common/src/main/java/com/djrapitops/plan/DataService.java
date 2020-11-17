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
package com.djrapitops.plan;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Service for sourcing, mapping and consuming data.
 * <p>
 * The service is in charge of two data flows:
 * - push, given to consumers
 * - pull, obtained from sources
 * <p>
 * The mappers facilitate a one way type transformation if needed.
 * <p>
 * The interface is very abstract about how data is obtained,
 * but here are my general ideas of where the abstraction is coming from.
 * - push cause one or multiple consumers to modify stored data
 * - pull cause one or multiple suppliers to fetch stored data
 * - mappers are stateless type transformers in memory
 * <p>
 * Example use-case:
 * - PlayerJoinEvent -> mapped to a generic event
 * - that generic event is then consumed and mapped until the data is in a database.
 * <p>
 * - Some kind of data is wanted to place on a web page
 * - It is requested and the suppliers and mappers give the wanted type of data.
 * <p>
 * Further research needed:
 * - Can this limited type system represent types of data that need parameters
 * (such as differentiate between two servers data)
 */
public interface DataService {

    <M> DataService push(Class<M> type, M data);

    <S> Optional<S> pull(Class<S> type);

    <S, P> Optional<S> pull(Class<S> type, P parameter);

    <A, B> B mapTo(Class<B> toType, A from);

    default <S, P> Optional<S> pull(Class<S> type, Class<P> parameterType) {
        return pull(type, () -> pull(parameterType).orElse(null));
    }

    default <S, P> Optional<S> pull(Class<S> type, Supplier<P> parameter) {
        return pull(type, parameter.get());
    }

    <A, B> DataService registerMapper(Class<A> typeA, Class<B> typeB, Function<A, B> mapper);

    <M> DataService registerConsumer(Class<M> type, Consumer<M> consumer);

    <S> DataService registerSupplier(Class<S> type, Supplier<S> supplier);

    <P, S> DataService registerSupplier(Class<S> type, Class<P> parameterType, Function<P, S> supplierWithParameter);

}
