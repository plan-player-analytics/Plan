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

import java.util.Optional;

public final class URLTarget {

    private final String full;

    public URLTarget(String target) {
        full = target;
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
     * Removes parts of the URL before an index.
     * <p>
     * Example: /example/target, 0 returns /example/target
     * Example: /example/target, 1 returns /target
     * Example: /example/target, 2 returns ''
     * Example: /, 0 returns /
     * Example: /, 1 returns ''
     *
     * @param from  URL String
     * @param index String is indexed based on slashes /0-index/1-index/2-index/etc
     * @return Substring based on the slash index.
     */
    static String removePartsBefore(String from, int index) {
        int applied = -1;
        String finding = from;
        int appliedSlash;

        // Find slash in the String by index
        while (applied < index) {
            appliedSlash = finding.indexOf('/');

            boolean hasNoSlash = appliedSlash == -1;
            if (hasNoSlash) return ""; // No more slashes

            finding = finding.substring(appliedSlash + 1);
            applied++;
        }
        return "/" + finding;
    }

    /**
     * Obtain part of the target by index of slashes in the URL.
     * <p>
     * Example: "/example/target", 0 returns "example"
     * Example: "/example/target", 1 returns "target"
     * Example: "/example/target", 2 returns empty optional
     * Example: "/example/target/", 2 returns ""
     * Example: "/", 0 returns ""
     * Example: "/", 1 returns empty optional
     *
     * @param index Index from root, eg. /0/1/2/3 etc
     * @return part after a '/' in the target,
     */
    public Optional<String> getPart(int index) {
        String leftover = removePartsBefore(full, index);
        if (leftover.isEmpty()) return Optional.empty();

        // Remove the leading slash to find ending slash
        leftover = leftover.substring(1);

        // Remove rest of the target (Ends in the next slash)
        int nextSlash = leftover.indexOf('/');
        if (nextSlash == -1) {
            return Optional.of(leftover);
        } else {
            return Optional.of(leftover.substring(0, nextSlash));
        }
    }

    public boolean endsWith(String suffix) {
        return full.endsWith(suffix);
    }

    /**
     * Immutable modification, removes first part of the target string.
     * <p>
     * Example: URLTarget "/example/target" return value of omitFirst URLTarget is "/target"
     * Example: URLTarget "/example" return value of omitFirst URLTarget is "/"
     * Example: URLTarget "/" return value of omitFirst URLTarget is ""
     *
     * @return new URLTarget with first part removed.
     */
    public URLTarget omitFirst() {
        return new URLTarget(removePartsBefore(full, 1));
    }
}
