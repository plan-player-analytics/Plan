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
package com.djrapitops.plan.delivery.domain.datatransfer.graphs;

import java.util.List;
import java.util.Objects;

/**
 * Represents multiple graphs of same type.
 *
 * @author AuroraLS3
 */
public class GraphCollection<T> {

    private final List<T> graphs;
    private final String color;

    public GraphCollection(List<T> graphs, String color) {
        this.graphs = graphs;
        this.color = color;
    }

    public List<T> getGraphs() {
        return graphs;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphCollection<?> that = (GraphCollection<?>) o;
        return Objects.equals(getGraphs(), that.getGraphs()) && Objects.equals(getColor(), that.getColor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGraphs(), getColor());
    }

    @Override
    public String toString() {
        return "GraphCollection{" +
                "graphs=" + graphs +
                ", color='" + color + '\'' +
                '}';
    }
}
