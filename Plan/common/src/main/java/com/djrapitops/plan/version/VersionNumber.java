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
package com.djrapitops.plan.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionNumber implements Comparable<VersionNumber> {
    private static final Pattern MATCH_NUMBERS = Pattern.compile("(\\d+)");

    private final String version;
    private final List<Long> versionNumbers;

    public VersionNumber(String version) {
        this.version = version;

        Matcher matches = MATCH_NUMBERS.matcher(version);
        versionNumbers = new ArrayList<>();
        while (matches.find()) {
            versionNumbers.add(Long.parseLong(matches.group()));
        }
    }

    public boolean isNewerThan(VersionNumber other) {
        return compareTo(other) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionNumber that = (VersionNumber) o;
        return version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public int compareTo(VersionNumber o) {
        int vSize = this.versionNumbers.size();
        int ovSize = o.versionNumbers.size();

        for (int i = 0; i < Math.min(vSize, ovSize); i++) {
            // Higher number goes first
            int comparison = o.versionNumbers.get(i).compareTo(this.versionNumbers.get(i));
            if (comparison != 0) return comparison;
        }

        if (vSize == ovSize || vSize != 0 && ovSize != 0) return 0;
        // If just one of the sizes is 0, have the one with some numbers go first
        return vSize > ovSize ? -1 : 1;
    }

    public String asString() {
        return version;
    }

    @Override
    public String toString() {
        return "VersionNumber{" +
                "version='" + version + '\'' +
                '}';
    }
}
