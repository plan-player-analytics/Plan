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
package com.djrapitops.plan.extension.icon;

import java.util.Objects;

/**
 * Object that represents an icon on the website.
 * <p>
 * See <a href="https://fontawesome.com/icons">FontAwesome</a> (select 'free')) for icons and their {@link Family}.
 *
 * @author AuroraLS3
 */
public class Icon {

    // Implementation detail, set during icon storage to optimize relation inserts.
    int id;

    private Family type;
    private String name;
    private Color color;

    private Icon() {
        type = Family.SOLID;
        color = Color.NONE;
    }

    public Icon(Family type, String name, Color color) {
        this.type = type;
        this.name = name;
        this.color = color;
    }

    public static Builder called(String name) {
        return new Builder().called(name);
    }

    public static Builder of(Family type) {
        return new Builder().of(type);
    }

    public static Builder of(Color color) {
        return new Builder().of(color);
    }

    public Family getFamily() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public Icon setColor(Color color) {
        this.color = color;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Icon icon = (Icon) o;
        return type == icon.type && Objects.equals(getName(), icon.getName()) && getColor() == icon.getColor();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, getName(), getColor());
    }

    @Override
    public String toString() {
        return "Icon{" + type.name() + ", '" + name + '\'' + ", " + color.name() + '}';
    }

    /**
     * Builder for an {@link Icon}.
     */
    public static class Builder {

        private final Icon icon;

        Builder() {
            this.icon = new Icon();
        }

        public Builder called(String name) {
            icon.name = name;
            return this;
        }

        public Builder of(Color color) {
            icon.color = color;
            return this;
        }

        public Builder of(Family type) {
            icon.type = type;
            return this;
        }

        public Icon build() {
            if (icon.name == null) {
                throw new IllegalStateException("'name' was not defined yet!");
            }
            return icon;
        }
    }
}
