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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method Annotation to determine that a method can not be called unless a condition is fulfilled.
 * <p>
 * Condition information is provided with {@link com.djrapitops.plan.extension.annotation.BooleanProvider}.
 * If {@link com.djrapitops.plan.extension.annotation.BooleanProvider} for the condition is not specified the
 * method tagged with this annotation will not be called, (Condition is assumed false).
 * <p>
 * Please note that Conditional does not cross method parameter boundaries - (Conditional on a player method does not
 * take into account conditionals of server).
 *
 * @author AuroraLS3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Conditional {

    /**
     * Name of the condition limited to 50 characters.
     *
     * @return Case sensitive string of max 50 characters.
     */
    String value();

    /**
     * Reverse the condition.
     * <p>
     * Example:
     * - Method with {@code Conditional("expires", negated = true)} will only be called when the condition "expires" is false.
     *
     * @return {@code false} by default.
     */
    boolean negated() default false;
}