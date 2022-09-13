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

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.implementation.providers.gathering.Conditions;

import java.util.*;
import java.util.function.Supplier;

public class ExtDataBuilder implements ExtensionDataBuilder {

    private final DataExtension extension;

    private final List<ClassValuePair> values;
    private final Set<String> invalidatedValues;

    public ExtDataBuilder(DataExtension extension) {
        this.extension = extension;
        values = new ArrayList<>();
        invalidatedValues = new HashSet<>();
    }

    @Override
    public ValueBuilder valueBuilder(String text) {
        if (text == null) throw new IllegalArgumentException("'text' can't be null");
        return new ExtValueBuilder(text, extension);
    }

    @Override
    public <T> ExtensionDataBuilder addValue(Class<T> ofType, DataValue<T> dataValue) {
        if (ofType != null && dataValue != null) values.add(new ClassValuePair(ofType, dataValue));
        return this;
    }

    @Override
    public <T> ExtensionDataBuilder addValue(Class<T> ofType, Supplier<DataValue<T>> dataValue) {
        try {
            if (ofType != null && dataValue != null) addValue(ofType, dataValue.get());
        } catch (NotReadyException | UnsupportedOperationException ignored) {
            // This exception is ignored by default to allow throwing errors inside the lambda to keep code clean.
        }
        // Other exceptions handled by ProviderValueGatherer during method call.
        return this;
    }

    @Override
    public ExtensionDataBuilder invalidateValue(String text) {
        invalidatedValues.add(ExtValueBuilder.formatTextAsIdentifier(text));
        return this;
    }

    public Set<String> getInvalidatedValues() {
        return invalidatedValues;
    }

    public List<ClassValuePair> getValues() {
        Collections.sort(values);
        return values;
    }

    public String getExtensionName() {
        return extension.getPluginName();
    }

    @Override
    public void addAll(ExtensionDataBuilder builder) {
        if (!(builder instanceof ExtDataBuilder)) return;
        // From same DataExtension
        if (!extension.getClass().equals(((ExtDataBuilder) builder).extension.getClass())) {
            throw new IllegalArgumentException("Can not combine data from two different extensions! (" +
                    extension.getClass().getName() + ',' + ((ExtDataBuilder) builder).extension.getClass().getName() + ")");
        }

        this.values.addAll(((ExtDataBuilder) builder).values);
        this.invalidatedValues.addAll(((ExtDataBuilder) builder).invalidatedValues);
    }

    public static final class ClassValuePair implements Comparable<ClassValuePair> {
        private final Class<?> type;
        private final DataValue<?> value;

        public <T> ClassValuePair(Class<T> type, DataValue<T> value) {
            this.type = type;
            this.value = value;
        }

        public <T> Optional<DataValue<T>> getValue(Class<T> ofType) {
            if (type.equals(ofType)) {
                return Optional.ofNullable((DataValue<T>) value);
            }
            return Optional.empty();
        }

        @Override
        public int compareTo(ClassValuePair that) {
            boolean thisIsBoolean = Boolean.class.isAssignableFrom(type) && value instanceof BooleanDataValue;
            boolean otherIsBoolean = Boolean.class.isAssignableFrom(that.type) && that.value instanceof BooleanDataValue;
            if (thisIsBoolean && !otherIsBoolean) {
                return -1; // This is boolean, have it go first
            } else if (!thisIsBoolean && otherIsBoolean) {
                return 1; // Other is boolean, have it go first
            } else if (thisIsBoolean) {
                // Both are Booleans, so they might provide a condition

                Optional<String> otherCondition = ((BooleanDataValue) that.value).getInformation().getCondition();
                String providedCondition = ((BooleanDataValue) value).getInformation().getProvidedCondition();
                // Another provider's required condition is satisfied by this, have this first
                if (otherCondition.filter(c -> Conditions.matchesCondition(c, providedCondition)).isPresent()) {
                    return -1;
                }

                // Required condition is satisfied by another provider, have that first
                Optional<String> condition = ((BooleanDataValue) value).getInformation().getCondition();
                String otherProvidedCondition = ((BooleanDataValue) that.value).getInformation().getProvidedCondition();
                if (condition.filter(c -> Conditions.matchesCondition(c, otherProvidedCondition)).isPresent()) {
                    return 1;
                }
            }
            // Irrelevant, keep where is
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassValuePair that = (ClassValuePair) o;
            return Objects.equals(type, that.type) && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, value);
        }

        @Override
        public String toString() {
            return "ClassValuePair{" +
                    "type=" + type +
                    ", value=" + value +
                    '}';
        }
    }
}
