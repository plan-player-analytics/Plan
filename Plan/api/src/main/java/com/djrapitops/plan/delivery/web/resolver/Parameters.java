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
package com.djrapitops.plan.delivery.web.resolver;

import java.util.Map;
import java.util.Optional;

/**
 * Represents URI parameters described with {@code ?param=value&param2=value2} in the URL.
 *
 * @author Rsl1122
 */
public final class Parameters {

    private final Map<String, String> byKey;

    public Parameters(Map<String, String> byKey) {
        this.byKey = byKey;
    }

    /**
     * Obtain an URI parameter by key.
     *
     * @param key Case-sensitive key, eg. 'param' in {@code ?param=value&param2=value2}
     * @return The value in the URL or empty if key is not specified in the URL.
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(byKey.get(key));
    }
}
