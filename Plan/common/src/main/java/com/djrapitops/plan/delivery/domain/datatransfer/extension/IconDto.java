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

import com.djrapitops.plan.delivery.rendering.html.icon.Icon;

public record IconDto(
        String family,
        String familyClass,
        String color,
        String colorClass,
        String iconName
) {

    public IconDto(com.djrapitops.plan.extension.icon.Icon extensionIcon) {
        this(Icon.fromExtensionIcon(extensionIcon));
    }

    private IconDto(Icon icon) {
        this(
                icon.getFamily().name(),
                icon.getFamily().getFamilyClass(),
                icon.getColor().name(),
                icon.getColor().getHtmlClass(),
                icon.getName()
        );
    }
}
