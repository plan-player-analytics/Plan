package com.djrapitops.plan.utilities.html.icon;

import com.djrapitops.plugin.utilities.Verify;

public class Icon {

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
            Verify.nullCheck(icon.name, () -> new IllegalStateException("'name' was not defined yet!"));
            return icon;
        }

        @Override
        public String toString() {
            return build().toHtml();
        }
    }
}
