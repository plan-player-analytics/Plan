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

import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.utilities.java.TriConsumer;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Singleton
public class DataSvc implements DataService {

    private final Lazy<DBSystem> dbSystem;

    private final Map<ClassPair, Function> pullSources;
    private final Map<Class, Supplier> noIdentifierPullSources;

    private final MultiHashMap<ClassPair, BiConsumer> sinks;

    private final MultiHashMap<ClassPair, Mapper> mappers;

    @Inject
    public DataSvc(
            Lazy<DBSystem> dbSystem
    ) {
        this.dbSystem = dbSystem;
        pullSources = new HashMap<>();
        noIdentifierPullSources = new HashMap<>();
        sinks = new MultiHashMap<>();
        mappers = new MultiHashMap<>();
    }

    @Override
    public <K, T> void push(K identifier, T value, Class<T> type) {
        ClassPair<K, T> classPair = new ClassPair<>((Class<K>) identifier.getClass(), type);
        for (BiConsumer<K, T> sink : sinks.get(classPair)) {
            sink.accept(identifier, value);
        }

        for (Mapper mapper : mappers.get(classPair)) {
            push(identifier, mapper.func.apply(identifier, value), mapper.typeB);
        }
    }

    @Override
    public <K, A, B> Optional<B> map(K identifier, A value, Class<B> toType) {
        ClassPair<K, A> classPair = new ClassPair<>((Class<K>) identifier.getClass(), (Class<A>) value.getClass());

        List<Mapper> candidates = this.mappers.get(classPair);
        return candidates
                .stream()
                .filter(mapper -> Objects.equals(mapper.typeB, toType))
                .findAny()
                .map(mapper -> toType.cast(mapper.func.apply(identifier, value)));
    }

    @Override
    public <K, A, B> DataService registerMapper(Class<K> identifierType, Class<A> from, Class<B> to, BiFunction<K, A, B> mapper) {
        ClassPair<K, A> classPair = new ClassPair<>(identifierType, from);
        mappers.putOne(classPair, new Mapper<>(from, to, mapper));
        return this;
    }

    @Override
    public <K, A, B> DataService registerMapper(Class<K> identifierType, Class<A> from, Class<B> to, Function<A, B> mapper) {
        return registerMapper(identifierType, from, to, (id, value) -> mapper.apply(value));
    }

    @Override
    public <K, Y, A, B> DataService registerMapper(Class<K> fromIdentifier, Class<A> from, Class<Y> toIdentifier, Class<B> to, TriConsumer<K, A, BiConsumer<Y, B>> mapper) {
        ClassPair<K, A> classPair = new ClassPair<>(fromIdentifier, from);
        sinks.putOne(classPair, (id, value) -> mapper.accept(fromIdentifier.cast(id), from.cast(value), this::push));
        return this;
    }

    @Override
    public <K, T> DataService registerSink(Class<K> identifierType, Class<T> type, BiConsumer<K, T> consumer) {
        ClassPair<K, T> classPair = new ClassPair<>(identifierType, type);
        sinks.putOne(classPair, consumer);
        return this;
    }

    @Override
    public <K, T> DataService registerDatabaseSink(Class<K> identifierType, Class<T> type, BiFunction<K, T, Transaction> consumer) {
        return registerSink(identifierType, type, (id, value) -> dbSystem.get().getDatabase().executeTransaction(consumer.apply(id, value)));
    }

    @Override
    public <K, T> Optional<T> pull(Class<T> type, K identifier) {
        ClassPair<K, T> classPair = new ClassPair<>((Class<K>) identifier.getClass(), type);
        return Optional.ofNullable(pullSources.get(classPair))
                .map(source -> source.apply(identifier))
                .map(type::cast);
    }

    @Override
    public <T> Optional<T> pullWithoutId(Class<T> type) {
        return Optional.ofNullable(noIdentifierPullSources.get(type))
                .map(Supplier::get)
                .map(type::cast);
    }

    @Override
    public <K, T> DataService registerPullSource(Class<K> identifierType, Class<T> type, Function<K, T> source) {
        ClassPair<K, T> classPair = new ClassPair<>(identifierType, type);
        pullSources.put(classPair, source);
        return this;
    }

    @Override
    public <K, T> DataService registerDatabasePullSource(Class<K> identifierType, Class<T> type, Function<K, Query<T>> source) {
        return registerPullSource(identifierType, type, identifier -> dbSystem.get().getDatabase().query(source.apply(identifier)));
    }

    @Override
    public <T> DataService registerPullSource(Class<T> type, Supplier<T> source) {
        noIdentifierPullSources.put(type, source);
        return this;
    }

    @Override
    public <T> DataService registerDatabasePullSource(Class<T> type, Supplier<Query<T>> source) {
        return registerPullSource(type, () -> dbSystem.get().getDatabase().query(source.get()));
    }

    private static class ClassPair<A, B> {
        final Class<A> a;
        final Class<B> b;

        public ClassPair(Class<A> a, Class<B> b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassPair<?, ?> classPair = (ClassPair<?, ?>) o;
            return a.equals(classPair.a) &&
                    b.equals(classPair.b);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }
    }

    private static class MultiHashMap<A, B> extends ConcurrentHashMap<A, List<B>> {

        void putOne(A key, B value) {
            List<B> values = getOrDefault(key, new ArrayList<>());
            values.add(value);
            put(key, values);
        }
    }

    private static class Mapper<K, A, B> {
        final Class<A> typeA;
        final Class<B> typeB;
        final BiFunction<K, A, B> func;

        public Mapper(Class<A> typeA, Class<B> typeB, BiFunction<K, A, B> func) {
            this.typeA = typeA;
            this.typeB = typeB;
            this.func = func;
        }
    }
}
