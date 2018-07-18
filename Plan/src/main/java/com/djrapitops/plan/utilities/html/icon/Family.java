package com.djrapitops.plan.utilities.html.icon;

public enum Family {
    SOLID("<i class=\"", " fa fa-", "\"></i>"),
    REGULAR("<i class=\"", " far fa-", "\"></i>"),
    BRAND("<i class=\"", " fab fa-", "\"></i>"),
    LINE("<i class=\"", " material-icons\">", "</i>");

    private final String prefix;
    private final String middle;
    private final String suffix;

    Family(String prefix, String middle, String suffix) {
        this.prefix = prefix;
        this.middle = middle;
        this.suffix = suffix;
    }

    public String appendAround(String color, String name) {
        return prefix + color + middle + name + suffix;
    }
}
