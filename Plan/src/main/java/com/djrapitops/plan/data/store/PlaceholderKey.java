package com.djrapitops.plan.data.store;

/**
 * Special Key object that can be used for placeholders when replacing values in html files.
 *
 * @author Rsl1122
 */
public class PlaceholderKey<T> extends Key<T> {

    private final String placeholder;

    public PlaceholderKey(Class<T> type, String placeholder) {
        super(type, placeholder);
        this.placeholder = placeholder;
    }

    public PlaceholderKey(Type<T> type, String placeholder) {
        super(type, placeholder);
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}