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
package com.djrapitops.plan.delivery.web.resolver.request;

import java.util.Optional;

public final class URIPath {

    // Example: /target/path/url
    private final String path;

    public URIPath(String path) {
        this.path = path;
    }

    /**
     * Removes parts of the URL before an index.
     * <p>
     * Example: /example/path, 0 returns /example/path
     * Example: /example/path, 1 returns /path
     * Example: /example/path, 2 returns ''
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
     * Obtain the full path.
     *
     * @return Example: "/target/path/in/url"
     */
    public String asString() {
        return path;
    }

    /**
     * Obtain part of the path by index of slashes in the URL.
     * <p>
     * Example: "/example/path", 0 returns "example"
     * Example: "/example/path", 1 returns "path"
     * Example: "/example/path", 2 returns empty optional
     * Example: "/example/path/", 2 returns ""
     * Example: "/", 0 returns ""
     * Example: "/", 1 returns empty optional
     *
     * @param index Index from root, eg. /0/1/2/3 etc
     * @return part after a '/' in the path,
     */
    public Optional<String> getPart(int index) {
        String leftover = removePartsBefore(path, index);
        if (leftover.isEmpty()) return Optional.empty();

        // Remove the leading slash to find ending slash
        leftover = leftover.substring(1);

        // Remove rest of the path (Ends in the next slash)
        int nextSlash = leftover.indexOf('/');
        if (nextSlash == -1) {
            return Optional.of(leftover);
        } else {
            return Optional.of(leftover.substring(0, nextSlash));
        }
    }

    public boolean endsWith(String suffix) {
        return path.endsWith(suffix);
    }

    public boolean startsWith(String prefix) {return path.startsWith(prefix);}

    /**
     * Immutable modification, removes first part of the path string.
     * <p>
     * Example: URIPath "/example/path" return value of omitFirst URIPath is "/path"
     * Example: URIPath "/example" return value of omitFirst URIPath is "/"
     * Example: URIPath "/" return value of omitFirst URIPath is ""
     *
     * @return new URIPath with first part removed.
     */
    public URIPath omitFirst() {
        return new URIPath(removePartsBefore(path, 1));
    }

    public int length() {
        int count = 0;
        for (char c : path.toCharArray()) {
            if (c == '/') count++;
        }
        return count;
    }

    @Override
    public String toString() {
        return "URIPath{" +
                "path='" + path + '\'' +
                '}';
    }
}
