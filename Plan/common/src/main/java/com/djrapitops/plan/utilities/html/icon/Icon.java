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
package com.djrapitops.plan.utilities.html.icon;

/**
 * @deprecated This Class exists to keep plugins that used PluginData from breaking.
 */
@Deprecated
public class Icon {

    private Family type;
    private String name;
    private Color color;

    private Icon() {
        type = Family.SOLID;
        color = Color.NONE;
    }

    /**
     * @deprecated This Class exists to keep plugins that used PluginData from breaking.
     */
    @Deprecated
    public Icon(Family type, String name, Color color) {
        this.type = type;
        this.name = name;
        this.color = color;
    }

    /**
     * @deprecated This Class exists to keep plugins that used PluginData from breaking.
     */
    @Deprecated
    public static Builder called(String name) {
        return new Builder().called(name);
    }

    public static Builder of(Family type) {
        return new Builder().of(type);
    }

    public static Builder of(Color color) {
        return new Builder().of(color);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String toHtml() {
        return type.appendAround(color.getHtmlClass(), name);
    }

    @Override
    public String toString() {
        return toHtml();
    }

    /**
     * @deprecated This Class exists to keep plugins that used PluginData from breaking.
     */
    @Deprecated
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
            return icon;
        }

        @Override
        public String toString() {
            return build().toHtml();
        }
    }
}
