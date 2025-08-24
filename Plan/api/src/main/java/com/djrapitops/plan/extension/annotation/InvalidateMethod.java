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
package com.djrapitops.plan.extension.annotation;

import java.lang.annotation.*;

/**
 * Annotation used to invalidate old method values.
 * <p>
 * The name of the methods are used as an identifier in the database, so that a single provider does not duplicate entries.
 * Only first 50 characters of the method name are stored.
 * If you need to change a method name add this class annotation with the old method name.
 *
 * @author AuroraLS3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(InvalidateMethod.Multiple.class)
public @interface InvalidateMethod {

    /**
     * Name of the old method, values of which should be removed from the database.
     *
     * @return Name of the old method, case sensitive. Only first 50 characters are used.
     */
    String value();

    /**
     * Multiple {@link InvalidateMethod} annotations are supported per class.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Multiple {

        /**
         * All the annotations.
         *
         * @return All InvalidateMethod annotations in the class.
         */
        InvalidateMethod[] value();

    }

}
