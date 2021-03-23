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
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Singleton
public class DataSvc implements DataService {

    private final MultiHashMap<Class, Mapper> mappers;
    private final MultiHashMap<Class, Mapper> mappersReverse;
    private final Map<Class, Supplier> suppliers;
    private final Map<ClassPair, Function> suppliersWithParameter;
    private final MultiHashMap<Class, Consumer> consumers;

    private final Lazy<DBSystem> dbSystem;

    @Inject
    public DataSvc(
            Lazy<DBSystem> dbSystem
    ) {
        this.dbSystem = dbSystem;
        mappers = new MultiHashMap<>();
        mappersReverse = new MultiHashMap<>();
        suppliers = new ConcurrentHashMap<>();
        suppliersWithParameter = new ConcurrentHashMap<>();
        consumers = new MultiHashMap<>();
    }

    @Override
    public <A> DataService push(Class<A> type, A data) {
        if (data == null) return this;
        List<Mapper> mappers = this.mappers.get(type);
        for (Mapper mapper : mappers) {
            push(mapper.typeB, mapper.func.apply(data));
        }
        List<Consumer> consumers = this.consumers.get(type);
        for (Consumer<A> consumer : consumers) {
            consumer.accept(data);
        }
        if (mappers.isEmpty() && consumers.isEmpty()) {
            System.out.println("WARN: Nothing consumed " + type);
        }
        return this;
    }

    @Override
    public <T> Optional<T> pull(Class<T> type) {
        Supplier<T> present = this.suppliers.get(type);
        if (present != null) return Optional.ofNullable(present.get());

        List<Mapper> mappers = this.mappersReverse.get(type);
        for (Mapper mapper : mappers) {
            Optional<T> found = pull(mapper.typeA).map(mapper.func);
            if (found.isPresent()) return found;
        }

        System.out.println("WARN: Nothing supplied " + type);
        return Optional.empty();
    }

    @Override
    public <A, B> B mapTo(Class<B> toType, A from) {
        List<Mapper> mappers = this.mappers.get(from.getClass());
        for (Mapper mapper : mappers) {
            if (mapper.typeB.equals(toType)) {
                return toType.cast(mapper.func);
            }
        }
        // TODO Figure out type mapping resolution when it needs more than one mapping
        System.out.println("WARN: No mapper for " + from.getClass() + " -> " + toType);
        return null;
    }

    @Override
    public <A, B> DataService registerMapper(Class<A> typeA, Class<B> typeB, Function<A, B> mapper) {
        Mapper<A, B> asWrapper = new Mapper<>(typeA, typeB, mapper);
        // TODO Prevent two mappers for same 2 types with a data structure
        mappers.putOne(typeA, asWrapper);
        mappersReverse.putOne(typeB, asWrapper);
        return this;
    }

    @Override
    public <A> DataService registerConsumer(Class<A> type, Consumer<A> consumer) {
        consumers.putOne(type, consumer);
        return this;
    }

    @Override
    public <A> DataService registerSupplier(Class<A> type, Supplier<A> supplier) {
        suppliers.put(type, supplier);
        return this;
    }

    @Override
    public <S, P> Optional<S> pull(Class<S> type, P parameter) {
        if (parameter == null) return Optional.empty();
        Function<P, S> function = suppliersWithParameter.get(new ClassPair<>(type, parameter.getClass()));
        return function != null ? Optional.of(function.apply(parameter)) : Optional.empty();
    }

    @Override
    public <P, S> DataService registerSupplier(Class<S> type, Class<P> parameterType, Function<P, S> supplierWithParameter) {
        suppliersWithParameter.put(new ClassPair<>(type, parameterType), supplierWithParameter);
        return this;
    }

    @Override
    public <P, S> DataService registerDBSupplier(Class<S> type, Class<P> parameterType, Function<P, Query<S>> queryVisitor) {
        return registerSupplier(type, parameterType, parameter -> dbSystem.get().getDatabase().query(queryVisitor.apply(parameter)));
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

    private static class KeyValuePair<K, V> {
        final K key;
        final V value;

        public KeyValuePair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private static class MultiHashMap<A, B> extends ConcurrentHashMap<A, List<B>> {

        void putOne(A key, B value) {
            List<B> values = getOrDefault(key, new ArrayList<>());
            values.add(value);
            put(key, values);
        }

    }

    private static class Mapper<A, B> {
        final Class<A> typeA;
        final Class<B> typeB;
        final Function<A, B> func;

        public Mapper(Class<A> typeA, Class<B> typeB, Function<A, B> func) {
            this.typeA = typeA;
            this.typeB = typeB;
            this.func = func;
        }
    }
}
