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

import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;

import java.lang.annotation.Annotation;

public abstract class AnnotationTextAndIconProvider<A extends Annotation, R, D> extends AnnotationDataProvider<A, R, D> {

    public AnnotationTextAndIconProvider(A provider, ExtensionMethod method, Class<R> returnType, Class<D> dataType) {
        super(provider, method, returnType, dataType);
    }

    @Override
    public ValueBuilder getValueBuilder(ExtensionDataBuilder dataBuilder) {
        return super.getValueBuilder(dataBuilder)
                .icon(iconName(), iconFamily(), iconColor());
    }

    public abstract String text();
    public abstract String iconName();
    public abstract Family iconFamily();
    public abstract Color iconColor();
}
