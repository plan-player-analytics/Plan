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

import com.djrapitops.plan.delivery.domain.datatransfer.ServerDto;

import java.util.List;
import java.util.Objects;

/**
 * Represents a line graph of some server so that they can be stacked.
 *
 * @author AuroraLS3
 */
public class ServerSpecificLineGraph {

    private final List<Number[]> points;
    private final ServerDto server;

    public ServerSpecificLineGraph(List<Number[]> points, ServerDto server) {
        this.points = points;
        this.server = server;
    }

    public List<Number[]> getPoints() {
        return points;
    }

    public ServerDto getServer() {
        return server;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerSpecificLineGraph that = (ServerSpecificLineGraph) o;
        return Objects.equals(getPoints(), that.getPoints()) && Objects.equals(getServer(), that.getServer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPoints(), getServer());
    }

    @Override
    public String toString() {
        return "ServerSpecificLineGraph{" +
                "points=" + points +
                ", server=" + server +
                '}';
    }
}
