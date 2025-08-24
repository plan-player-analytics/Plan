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
package com.djrapitops.plan.delivery.domain.datatransfer;

import com.djrapitops.plan.delivery.domain.PluginHistoryMetadata;

import java.util.List;
import java.util.Objects;

/**
 * History of plugin versions, sorted most recent first.
 *
 * @author AuroraLS3
 */
public class PluginHistoryDto {

    private final List<PluginHistoryMetadata> history;

    public PluginHistoryDto(List<PluginHistoryMetadata> history) {
        this.history = history;
    }

    public List<PluginHistoryMetadata> getHistory() {
        return history;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginHistoryDto that = (PluginHistoryDto) o;
        return Objects.equals(getHistory(), that.getHistory());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHistory());
    }

    @Override
    public String toString() {
        return "PluginHistoryDto{" +
                "history=" + history +
                '}';
    }
}
