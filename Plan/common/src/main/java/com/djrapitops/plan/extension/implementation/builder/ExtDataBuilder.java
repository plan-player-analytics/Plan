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
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.implementation.providers.gathering.Conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ExtDataBuilder implements ExtensionDataBuilder {

    private final List<ClassValuePair> values;
    private final DataExtension extension;

    public ExtDataBuilder(DataExtension extension) {
        this.extension = extension;
        values = new ArrayList<>();
    }

    @Override
    public ValueBuilder valueBuilder(String text) {
        return new ExtValueBuilder(text, extension);
    }

    @Override
    public <T> ExtensionDataBuilder addValue(Class<T> ofType, DataValue<T> dataValue) {
        values.add(new ClassValuePair(ofType, dataValue));
        return this;
    }

    @Override
    public <T> ExtensionDataBuilder addValue(Class<T> ofType, Supplier<DataValue<T>> dataValue) {
        try {
            values.add(new ClassValuePair(ofType, dataValue.get()));
        } catch (NotReadyException | UnsupportedOperationException ignored) {
            // This exception is ignored by default to allow throwing errors inside the lambda to keep code clean.
        }
        // Other exceptions handled by ProviderValueGatherer during method call.
        return this;
    }

    public List<ClassValuePair> getValues() {
        Collections.sort(values);
        return values;
    }

    public String getExtensionName() {
        return Optional.ofNullable(extension.getClass().getAnnotation(PluginInfo.class))
                .map(PluginInfo::name)
                .orElseThrow(() -> new IllegalArgumentException(extension.getClass().getName() + " does not have @PluginInfo annotation!"));
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
            if (Boolean.class.isAssignableFrom(type) && value instanceof BooleanDataValue) {
                if (Boolean.class.isAssignableFrom(that.type) && that.value instanceof BooleanDataValue) {
                    Optional<String> otherCondition = ((BooleanDataValue) that.value).getInformation().getCondition();
                    String providedCondition = ((BooleanDataValue) value).getInformation().getProvidedCondition();
                    // Another provider's required condition is satisfied by this, have this first
                    if (otherCondition.filter(c -> Conditions.matchesCondition(c, providedCondition)).isPresent()) {
                        return 1;
                    }

                    // Required condition is satisfied by another provider, have that first
                    Optional<String> condition = ((BooleanDataValue) value).getInformation().getCondition();
                    String otherProvidedCondition = ((BooleanDataValue) that.value).getInformation().getProvidedCondition();
                    if (condition.filter(c -> Conditions.matchesCondition(c, otherProvidedCondition)).isPresent()) {
                        return -1;
                    }
                }
            }
            return 0;
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
