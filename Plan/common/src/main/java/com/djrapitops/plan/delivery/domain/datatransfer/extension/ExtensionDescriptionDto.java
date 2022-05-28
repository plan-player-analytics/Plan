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

public class ExtensionDescriptionDto {

    private final String name;
    private final String text;
    private final String description;
    private final IconDto icon;
    private final int priority;

    public ExtensionDescriptionDto(ExtensionDescription description) {
        this.name = description.getName();
        this.text = description.getText();
        this.description = description.getDescription().orElse(null);
        this.icon = new IconDto(description.getIcon());
        this.priority = description.getPriority();
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public IconDto getIcon() {
        return icon;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionDescriptionDto that = (ExtensionDescriptionDto) o;
        return getPriority() == that.getPriority() && Objects.equals(getName(), that.getName()) && Objects.equals(getText(), that.getText()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getIcon(), that.getIcon());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getText(), getDescription(), getIcon(), getPriority());
    }

    @Override
    public String toString() {
        return "ExtensionDescriptionDto{" +
                "name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", description='" + description + '\'' +
                ", icon=" + icon +
                ", priority=" + priority +
                '}';
    }
}
