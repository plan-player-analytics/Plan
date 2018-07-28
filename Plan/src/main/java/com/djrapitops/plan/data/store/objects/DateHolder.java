package com.djrapitops.plan.data.store.objects;

/**
 * Interface for objects that have a epoch ms date.
 *
 * @author Rsl1122
 */
public interface DateHolder {

    /**
     * Get the date the object holds.
     *
     * @return Epoch ms - milliseconds passed since January 1st 1970.
     */
    long getDate();

}
