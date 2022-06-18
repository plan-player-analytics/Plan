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

import com.djrapitops.plan.extension.implementation.results.ExtensionDescription;

import java.util.Objects;

public class ExtensionValueDataDto {

    private final ExtensionDescriptionDto description;
    private final String type;
    private final Object value;

    public ExtensionValueDataDto(ExtensionDescription description, String type, Object value) {
        this.description = new ExtensionDescriptionDto(description);
        this.type = type;
        this.value = value;
    }

    public ExtensionDescriptionDto getDescription() {
        return description;
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionValueDataDto that = (ExtensionValueDataDto) o;
        return Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDescription(), getValue());
    }

    @Override
    public String toString() {
        return "ExtensionValueDataDto{" +
                "description=" + description +
                ", value=" + value +
                '}';
    }
}
