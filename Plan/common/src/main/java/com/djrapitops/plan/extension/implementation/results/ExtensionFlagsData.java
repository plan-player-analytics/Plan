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
package com.djrapitops.plan.extension.implementation.results;

import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.statistic.Flags;

import java.util.Objects;

public record ExtensionFlagsData(String providerName, String providerText, Flags flags,
                                 Icon icon) implements Comparable<ExtensionFlagsData> {

    public Color getTableColor() {
        return icon.getColor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionFlagsData that)) return false;
        return Objects.equals(providerName, that.providerName) &&
                Objects.equals(providerText, that.providerText) &&
                Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerName, providerText, icon);
    }

    @Override
    public int compareTo(ExtensionFlagsData other) {
        return String.CASE_INSENSITIVE_ORDER.compare(providerName, other.providerName);
    }
}