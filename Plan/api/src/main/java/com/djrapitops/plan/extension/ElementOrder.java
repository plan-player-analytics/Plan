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
package com.djrapitops.plan.extension;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum representing big elements of a plugin.
 * <p>
 * Used for determining in which order elements are placed on a {@link com.djrapitops.plan.extension.annotation.Tab} by
 * using {@link com.djrapitops.plan.extension.annotation.TabInfo}.
 *
 * @author AuroraLS3
 */
public enum ElementOrder {
    /**
     * Represents text - value pair box.
     */
    VALUES,
    /**
     * Represents graphs.
     */
    GRAPH,
    /**
     * Represents tables.
     */
    TABLE;

    public static String serialize(ElementOrder[] order) {
        StringBuilder builder = new StringBuilder();

        int length = order.length;
        for (int i = 0; i < length; i++) {
            builder.append(order[i].name());
            if (i < length - 1) {
                builder.append(',');
            }
        }

        return builder.toString();
    }

    public static ElementOrder[] deserialize(String serializedOrder) {
        if (serializedOrder == null || serializedOrder.isEmpty()) {
            return null;
        }

        String[] split = serializedOrder.split(",");

        List<ElementOrder> order = new ArrayList<>();
        for (String elementName : split) {
            try {
                ElementOrder element = valueOf(elementName);
                order.add(element);
            } catch (IllegalArgumentException ignore) {
                /* Has been deleted */
            }
        }

        return order.toArray(new ElementOrder[0]);
    }
}
