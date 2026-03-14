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
package com.djrapitops.plan.delivery.rendering.json.graphs.pie;

import java.util.List;
import java.util.Objects;

/**
 * @author AuroraLS3
 */
public record PieDrilldown(String name, String id, List<List<Object>> data) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PieDrilldown that = (PieDrilldown) o;
        return Objects.equals(name(), that.name()) && Objects.equals(id(), that.id()) && Objects.equals(data(), that.data());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name(), id(), data());
    }

    @Override
    public String toString() {
        return "PieDrilldown{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", data=" + data +
                '}';
    }
}
