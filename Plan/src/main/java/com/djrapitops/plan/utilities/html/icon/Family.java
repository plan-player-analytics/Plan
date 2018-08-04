package com.djrapitops.plan.utilities.html.icon;

public enum Family {
    SOLID(" fa fa-", "\"></i>"),
    REGULAR(" far fa-", "\"></i>"),
    BRAND(" fab fa-", "\"></i>"),
    LINE(" material-icons\">", "</i>");

    private final String middle;
    private final String suffix;

    Family(String middle, String suffix) {
        this.middle = middle;
        this.suffix = suffix;
    }

    public String appendAround(String color, String name) {
        return "<i class=\"" + color + middle + name + suffix;
    }
}
