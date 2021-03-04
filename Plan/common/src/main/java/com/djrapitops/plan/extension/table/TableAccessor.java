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
package com.djrapitops.plan.extension.table;

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;

/**
 * Utility for accessing implementation variables inside {@link Table.Factory} object.
 *
 * @author AuroraLS3
 */
public class TableAccessor {

    private TableAccessor() {
        /* Static method class */
    }

    public static Color getColor(Table.Factory factory) {
        return factory.color;
    }

    public static void setColor(Table.Factory factory, Color color) {
        factory.color = color;
    }

    public static String getTableName(Table.Factory factory) {
        return factory.tableName;
    }

    public static void setTableName(Table.Factory factory, String tableName) {
        factory.tableName = tableName;
    }

    public static String getTabName(Table.Factory factory) {
        return factory.tabName;
    }

    public static void setTabName(Table.Factory factory, String tabName) {
        factory.tabName = tabName;
    }

    public static int getTabPriority(Table.Factory factory) {
        return factory.tabPriority;
    }

    public static void setTabPriority(Table.Factory factory, int tabPriority) {
        factory.tabPriority = tabPriority;
    }

    public static ElementOrder[] getTabOrder(Table.Factory factory) {
        return factory.tabOrder;
    }

    public static void setTabOrder(Table.Factory factory, ElementOrder[] tabOrder) {
        factory.tabOrder = tabOrder;
    }

    public static Icon getTabIcon(Table.Factory factory) {
        return factory.tabIcon;
    }

    public static void setTabIcon(Table.Factory factory, Icon tabIcon) {
        factory.tabIcon = tabIcon;
    }
}