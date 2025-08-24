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
 * Method annotation to provide a String value.
 * <p>
 * Usage: {@code @StringProvider String method(UUID playerUUID)}
 * <p>
 * The returned value is limited to 100 characters, remainder will be clipped.
 * <p>
 * If the value is a player name, provide value for playerName=true.
 * This will allow linking between pages.
 *
 * @author AuroraLS3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StringProvider {

    /**
     * Text displayed before the value, limited to 50 characters.
     * <p>
     * Should inform the user what the value represents, for example
     * "Town Name", "Pet Name"
     *
     * @return String of max 50 characters, remainder will be clipped.
     */
    String text();

    /**
     * Display-priority of the value, highest value is placed top most.
     * <p>
     * Two values with same priority may appear in a random order.
     *
     * @return Priority between 0 and {@code Integer.MAX_VALUE}.
     */
    int priority() default 0;

    /**
     * Text displayed when hovering over the value, limited to 150 characters.
     * <p>
     * Should be used to clarify what the value is if not self evident, for example
     * text: "Power", description: "Faction power, affects ability of faction to perform actions. Regenerates"
     *
     * @return String of max 150 characters, remainder will be clipped.
     */
    String description() default "";

    /**
     * Determine if this value represents a Player name, for example a mayor of a town.
     *
     * @return {@code true} if the name can be used as a link to another player's page.
     */
    boolean playerName() default false;

    /**
     * Name of Font Awesome icon.
     * <p>
     * See <a href="https://fontawesome.com/icons">FontAwesome</a> (select 'free')) for icons and their {@link Family}.
     *
     * @return Name of the icon, if name is not valid no icon is shown.
     */
    String iconName() default "question";

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
    Color iconColor() default Color.NONE;

    /**
     * When the parameter is set to {@code true} the value from this Provider is shown on a table alongside players.
     *
     * @return false by default.
     */
    boolean showInPlayerTable() default false;
}
