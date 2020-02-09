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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class URLTarget {

    // Internal representation
    private final List<String> parts;
    private final String full;

    public URLTarget(String target) {
        this.parts = parse(target);
        full = target;
    }

    private List<String> parse(String target) {
        String[] partArray = target.split("/");
        // Ignores index 0, assuming target starts with /
        return Arrays.asList(partArray)
                .subList(1, partArray.length);
    }

    /**
     * Obtain the full target.
     *
     * @return Example: "/target/path/in/url"
     */
    public String asString() {
        return full;
    }

    /**
     * Obtain part of the target by index of slashes in the URL.
     *
     * @param index Index from root, eg. /0/1/2/3 etc
     * @return part after a '/' in the target, Example "target" for 0 in "/target/example", "" for 0 in "/", "" for 1 in "/example/"
     */
    public Optional<String> getPart(int index) {
        if (index >= parts.size()) {
            return Optional.empty();
        }
        return Optional.of(parts.get(index));
    }

    public boolean endsWith(String suffix) {
        return full.endsWith(suffix);
    }
}
