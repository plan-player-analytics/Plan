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

import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.utilities.java.TriConsumer;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface DataService {

    default <K, T> void push(K identifier, T value) {
        push(identifier, value, (Class<T>) value.getClass());
    }

    <K, T> void push(K identifier, T value, Class<T> type);

    default <K, A, B> DataService registerOptionalMapper(Class<K> identifierType, Class<A> from, Class<B> to, BiFunction<K, A, Optional<B>> mapper) {
        return registerMapper(identifierType, from, to, (id, value) -> mapper.apply(id, value).orElse(null));
    }

    <K, A, B> DataService registerMapper(Class<K> identifierType, Class<A> from, Class<B> to, BiFunction<K, A, B> mapper);

    <K, A, B> DataService registerMapper(Class<K> identifierType, Class<A> from, Class<B> to, Function<A, B> mapper);

    default <K, A, B> DataService registerDataServiceMapper(Class<K> identifierType, Class<A> from, Class<B> to, BiFunction<DataService, A, B> mapper) {
        return registerMapper(identifierType, from, to, value -> mapper.apply(this, value));
    }

    <K, Y, A, B> DataService registerMapper(Class<K> fromIdentifier, Class<A> from, Class<Y> toIdentifier, Class<B> to, TriConsumer<K, A, BiConsumer<Y, B>> mapper);

    <K, T> DataService registerSink(Class<K> identifierType, Class<T> type, BiConsumer<K, T> consumer);

    <K, T> DataService registerDatabaseSink(Class<K> identifierType, Class<T> type, BiFunction<K, T, Transaction> consumer);

    <K, T> Optional<T> pull(Class<T> type, K identifier);

    <T> Optional<T> pullWithoutId(Class<T> type);

    <K, T> DataService registerPullSource(Class<K> identifierType, Class<T> type, Function<K, T> source);

    default <K, T> DataService registerOptionalPullSource(Class<K> identifierType, Class<T> type, Function<K, Optional<T>> source) {
        return registerPullSource(identifierType, type, id -> source.apply(id).orElse(null));
    }

    <K, T> DataService registerDatabasePullSource(Class<K> identifierType, Class<T> type, Function<K, Query<T>> source);

    <T> DataService registerPullSource(Class<T> type, Supplier<T> source);

    <T> DataService registerDatabasePullSource(Class<T> type, Supplier<Query<T>> source);

    <K, A, B> Optional<B> map(K identifier, A value, Class<B> toType);

    interface Pipeline {
        void register(DataService service);
    }

}
