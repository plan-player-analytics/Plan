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

import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class Annotation for informing Plan about a plugin.
 *
 * @author AuroraLS3
 * @see TabOrder to determine preferred tab ordering if you use {@link Tab}s.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginInfo {

    /**
     * Name of the plugin, limited to 50 characters.
     *
     * @return String of max 50 characters, remainder will be clipped.
     */
    String name();

    /**
     * Name of Font Awesome icon.
     * <p>
     * See <a href="https://fontawesome.com/icons">FontAwesome</a> (select 'free')) for icons and their {@link Family}.
     *
     * @return Name of the icon, if name is not valid no icon is shown.
     */
    String iconName() default "cube";

    /**
     * Family of Font Awesome icon.
     * <p>
     * See <a href="https://fontawesome.com/icons">FontAwesome</a> (select 'free')) for icons and their {@link Family}.
     *
     * @return Family that matches an icon, if there is no icon for this family no icon is shown.
     */
    Family iconFamily() default Family.SOLID;

    /**
     * Color preference of the plugin.
     * <p>
     * This color will be set as the default color to use for plugin's elements.
     *
     * @return Preferred color. If none are specified defaults are used.
     */
    Color color() default Color.NONE;
}
