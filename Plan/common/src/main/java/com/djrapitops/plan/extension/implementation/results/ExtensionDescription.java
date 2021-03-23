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
package com.djrapitops.plan.extension.implementation.results;

import com.djrapitops.plan.extension.icon.Icon;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * Describes information about an extension value given by a Provider method.
 *
 * @author AuroraLS3
 */
public class ExtensionDescription implements Comparable<ExtensionDescription> {

    private final String name;
    private final String text;
    private final String description; // can be null
    private final Icon icon;
    private final int priority;

    public ExtensionDescription(String name, String text, String description, Icon icon, int priority) {
        this.name = name;
        this.text = text;
        this.description = description;
        this.icon = icon;
        this.priority = priority;
    }

    public String getName() {
        return StringUtils.truncate(name, 50);
    }

    public String getText() {
        return StringUtils.truncate(text, 50);
    }

    public Optional<String> getDescription() {
        return description == null || description.isEmpty() ? Optional.empty() : Optional.of(StringUtils.truncate(description, 150));
    }

    public Icon getIcon() {
        return icon;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(ExtensionDescription other) {
        return Integer.compare(other.priority, this.priority); // Higher is first
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionDescription)) return false;
        ExtensionDescription that = (ExtensionDescription) o;
        return priority == that.priority &&
                name.equals(that.name) &&
                text.equals(that.text) &&
                Objects.equals(description, that.description) &&
                icon.equals(that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, text, description, icon, priority);
    }

    @Override
    public String toString() {
        return "ExtensionDescription{" +
                "name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", description='" + description + '\'' +
                ", icon=" + icon +
                ", priority=" + priority +
                '}';
    }
}