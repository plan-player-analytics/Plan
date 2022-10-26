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
package com.djrapitops.plan.extension.extractor.dataprovider;

import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.annotation.Tab;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * A helper class for accessing data from a Provider annotation, as well as storing the {@link ExtensionMethod}.
 * @param <A> the annotation type
 * @param <R> the return type (to check if the return type is correct)
 * @param <D> the data type
 */
public abstract class AnnotationDataProvider<A extends Annotation, R, D> {

    protected final A provider;
    protected final ExtensionMethod method;
    protected final Class<R> returnType;
    protected final Class<D> dataType;

    public AnnotationDataProvider(A provider, ExtensionMethod method, Class<R> returnType, Class<D> dataType) {
        this.provider = provider;
        this.method = method;
        this.returnType = returnType;
        this.dataType = dataType;
    }

    public final A getProvider() {
        return provider;
    }

    public final ExtensionMethod getExtensionMethod() {
        return method;
    }

    public final Class<R> getReturnType() {
        return returnType;
    }

    public final Class<D> getDataType() {
        return dataType;
    }

    public String getBuilderName() {
        return method.getMethodName();
    }

    public ValueBuilder getValueBuilder(ExtensionDataBuilder dataBuilder) {
        return dataBuilder.valueBuilder(getBuilderName())
                .methodName(method)
                .conditional(method.getAnnotationOrNull(Conditional.class))
                .showOnTab(method.getAnnotationOrNull(Tab.class));
    }

    public abstract DataValue<D> addDataValueToBuilder(ValueBuilder builder, Supplier<D> dataSupplier);

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AnnotationDataProvider)) {
            return false;
        }
        AnnotationDataProvider<?, ?, ?> other = (AnnotationDataProvider<?, ?, ?>) obj;
        return super.equals(obj)
                && provider == other.provider
                && method == other.method
                && returnType == other.returnType
                && dataType == other.dataType;
    }
}
