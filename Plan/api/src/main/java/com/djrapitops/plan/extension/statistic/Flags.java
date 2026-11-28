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
package com.djrapitops.plan.extension.statistic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Return value for {@link com.djrapitops.plan.extension.annotation.FlagsProvider}.
 * <p>
 * Stores named Boolean flags. A null value represents a missing flag value, but its name is retained
 * so that it can be displayed as missing.
 */
public final class Flags {

    private final Map<String, Boolean> valueByKey;

    public Flags() {
        valueByKey = new HashMap<>();
    }

    public static Flags create() {
        return new Flags();
    }

    /**
     * Place a flag in the object.
     *
     * @param key   Flag name.
     * @param value Boolean value, or null to represent a missing value.
     * @return This object.
     */
    public Flags put(String key, Boolean value) {
        valueByKey.put(key, value);
        return this;
    }

    /**
     * Place flags in the object.
     *
     * @param flags String-Boolean map. Null values represent missing flags.
     * @return This object.
     */
    public Flags putAll(Map<String, Boolean> flags) {
        valueByKey.putAll(flags);
        return this;
    }

    /**
     * Place flags with value 'true' in the object.
     *
     * @param trueFlags List of flag names
     * @return This object.
     */
    public Flags addAllTrue(Collection<String> trueFlags) {
        trueFlags.forEach(value -> valueByKey.put(value, true));
        return this;
    }

    /**
     * Place flags with value 'false' in the object.
     *
     * @param falseFlags List of flag names
     * @return This object.
     */
    public Flags addAllFalse(Collection<String> falseFlags) {
        falseFlags.forEach(value -> valueByKey.put(value, false));
        return this;
    }

    /**
     * The method Plan uses to get access to the data.
     *
     * @return Values stored in this object.
     */
    public Map<String, Boolean> getValueByKey() {
        return valueByKey;
    }

    public boolean isEmpty() {
        return valueByKey.isEmpty();
    }
}
