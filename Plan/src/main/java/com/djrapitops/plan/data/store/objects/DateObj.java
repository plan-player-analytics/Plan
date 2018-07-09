package com.djrapitops.plan.data.store.objects;

/**
 * Object that has a value tied to a date.
 *
 * @author Rsl1122
 */
public class DateObj<T> implements DateHolder {

    private final long date;
    private final T value;

    public DateObj(long date, T value) {
        this.date = date;
        this.value = value;
    }

    @Override
    public long getDate() {
        return date;
    }

    public T getValue() {
        return value;
    }
}