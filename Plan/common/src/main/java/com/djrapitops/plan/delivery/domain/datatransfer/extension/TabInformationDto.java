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
import org.jspecify.annotations.Nullable;

import java.util.List;

public record TabInformationDto(
        String tabName,
        @Nullable IconDto icon,
        @Nullable List<ElementOrder> elementOrder, // can miss values
        int tabPriority
) {

    public TabInformationDto(TabInformation tabInformation) {
        this(
                tabInformation.getTabName(),
                tabInformation.getTabIcon() != null ? new IconDto(tabInformation.getTabIcon()) : null,
                tabInformation.getTabElementOrder(),
                tabInformation.getTabPriority()
        );
    }
}
