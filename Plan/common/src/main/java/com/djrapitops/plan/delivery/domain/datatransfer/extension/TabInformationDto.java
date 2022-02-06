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
package com.djrapitops.plan.delivery.domain.datatransfer.extension;

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.implementation.TabInformation;

import java.util.List;
import java.util.Objects;

public class TabInformationDto {

    private final String tabName;
    private final IconDto icon; // can be null
    private final List<ElementOrder> elementOrder; // can be null / miss values
    private final int tabPriority;

    public TabInformationDto(TabInformation tabInformation) {
        tabName = tabInformation.getTabName();
        icon = tabInformation.getTabIcon() != null ? new IconDto(tabInformation.getTabIcon()) : null;
        elementOrder = tabInformation.getTabElementOrder();
        tabPriority = tabInformation.getTabPriority();
    }

    public String getTabName() {
        return tabName;
    }

    public IconDto getIcon() {
        return icon;
    }

    public List<ElementOrder> getElementOrder() {
        return elementOrder;
    }

    public int getTabPriority() {
        return tabPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TabInformationDto that = (TabInformationDto) o;
        return getTabPriority() == that.getTabPriority() && Objects.equals(getTabName(), that.getTabName()) && Objects.equals(getIcon(), that.getIcon()) && Objects.equals(getElementOrder(), that.getElementOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTabName(), getIcon(), getElementOrder(), getTabPriority());
    }

    @Override
    public String toString() {
        return "TabInformationDto{" +
                "tabName='" + tabName + '\'' +
                ", icon=" + icon +
                ", elementOrder=" + elementOrder +
                ", tabPriority=" + tabPriority +
                '}';
    }
}
