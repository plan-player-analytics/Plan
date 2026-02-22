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
package com.djrapitops.plan.extension.implementation;

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a tab of {@link com.djrapitops.plan.extension.DataExtension} defined by {@link com.djrapitops.plan.extension.annotation.Tab} and
 * {@link com.djrapitops.plan.extension.annotation.TabInfo} annotations.
 *
 * @author AuroraLS3
 */
public class TabInformation {

    private final String tabName;
    private final Icon icon; // can be null
    private final List<ElementOrder> elementOrder; // can be null / miss values
    private final int tabPriority;

    public TabInformation(String tabName, Icon icon, ElementOrder[] elementOrder, int tabPriority) {
        this(
                tabName,
                icon,
                createElementOrderList(elementOrder),
                tabPriority
        );
    }

    public TabInformation(String tabName, Icon icon, List<ElementOrder> elementOrder, int tabPriority) {
        this.tabName = tabName;
        this.icon = icon;
        this.elementOrder = elementOrder;
        this.tabPriority = tabPriority;
    }

    private static List<ElementOrder> createElementOrderList(ElementOrder[] elementOrder) {
        List<ElementOrder> list = new ArrayList<>();
        if (elementOrder != null) {
            Collections.addAll(list, elementOrder);
        }
        return list;
    }

    public static Icon defaultIcon() {
        return new Icon(Family.SOLID, "circle", Color.NONE);
    }

    public String getTabName() {
        return StringUtils.truncate(tabName, 50);
    }

    public Icon getTabIcon() {
        return icon != null ? icon : defaultIcon();
    }

    public int getTabPriority() {
        return tabPriority;
    }

    public List<ElementOrder> getTabElementOrder() {
        if (elementOrder.isEmpty()) {
            return ElementOrder.valuesAsList();
        }

        ElementOrder[] possibleValues = ElementOrder.values();
        if (elementOrder.size() < possibleValues.length) {
            addMissingElements(possibleValues);
        }

        return elementOrder;
    }

    private void addMissingElements(ElementOrder[] possibleValues) {
        for (ElementOrder possibleValue : possibleValues) {
            if (!elementOrder.contains(possibleValue)) {
                elementOrder.add(possibleValue);
            }
        }
    }

    @Override
    public String toString() {
        return "TabInformation{" +
                "tabName='" + tabName + '\'' +
                ", icon=" + icon +
                ", elementOrder=" + elementOrder +
                ", tabPriority=" + tabPriority +
                '}';
    }

    public String getSerializedTabElementOrder() {
        return new TextStringBuilder().appendWithSeparators(getTabElementOrder(), ",").get();
    }
}