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

import java.util.Objects;

public class IconDto {

    private final String family;
    private final String familyClass;
    private final String color;
    private final String colorClass;
    private final String iconName;

    public IconDto(com.djrapitops.plan.extension.icon.Icon extensionIcon) {
        Icon icon = Icon.fromExtensionIcon(extensionIcon);
        family = icon.getFamily().name();
        familyClass = icon.getFamily().getFamilyClass();
        color = icon.getColor().name();
        colorClass = icon.getColor().getHtmlClass();
        iconName = icon.getName();
    }

    public String getFamily() {
        return family;
    }

    public String getFamilyClass() {
        return familyClass;
    }

    public String getColor() {
        return color;
    }

    public String getColorClass() {
        return colorClass;
    }

    public String getIconName() {
        return iconName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IconDto iconDto = (IconDto) o;
        return Objects.equals(getFamily(), iconDto.getFamily()) && Objects.equals(getFamilyClass(), iconDto.getFamilyClass()) && Objects.equals(getColor(), iconDto.getColor()) && Objects.equals(getColorClass(), iconDto.getColorClass()) && Objects.equals(getIconName(), iconDto.getIconName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFamily(), getFamilyClass(), getColor(), getColorClass(), getIconName());
    }

    @Override
    public String toString() {
        return "IconDto{" +
                "family='" + family + '\'' +
                ", familyClass='" + familyClass + '\'' +
                ", color='" + color + '\'' +
                ", colorClass='" + colorClass + '\'' +
                ", iconName='" + iconName + '\'' +
                '}';
    }
}
