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

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.icon.Family;

import java.lang.annotation.*;

/**
 * Class Annotation that allows determining an Icon and {@link ElementOrder} of a tab.
 *
 * @author AuroraLS3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(TabInfo.Multiple.class)
public @interface TabInfo {

    /**
     * Name of the tab this information is about.
     *
     * @return Tab name, limited to 50 characters.
     */
    String tab();

    /**
     * Name of Font Awesome icon.
     * <p>
     * See <a href="https://fontawesome.com/icons">FontAwesome</a> (select 'free')) for icons and their {@link Family}.
     *
     * @return Name of the icon, if name is not valid no icon is shown.
     */
    String iconName() default "circle";

    /**
     * Family of Font Awesome icon.
     * <p>
     * See <a href="https://fontawesome.com/icons">FontAwesome</a> (select 'free')) for icons and their {@link Family}.
     *
     * @return Family that matches an icon, if there is no icon for this family no icon is shown.
     */
    Family iconFamily() default Family.SOLID;

    /**
     * Order preference for the large elements of a tab.
     * <p>
     * If an ordering is not present they will be added to the end in the default order.
     * If a duplicate ordering exists only the first will be used for determining the order.
     *
     * @return ElementOrders in the order that they want to be displayed in.
     */
    ElementOrder[] elementOrder();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Multiple {
        TabInfo[] value();
    }
}
