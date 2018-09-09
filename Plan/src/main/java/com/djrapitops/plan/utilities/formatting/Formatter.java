package com.djrapitops.plan.utilities.formatting;

import java.util.function.Function;

/**
 * Interface for formatting a value into a String.
 *
 * @author Rsl1122
 */
public interface Formatter<T> extends Function<T, String> {

}
