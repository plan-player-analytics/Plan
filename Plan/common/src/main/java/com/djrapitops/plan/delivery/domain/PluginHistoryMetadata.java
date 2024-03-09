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
package com.djrapitops.plan.delivery.domain;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents plugin version history.
 * <p>
 * If version is null the plugin was uninstalled at that time.
 *
 * @author AuroraLS3
 */
public class PluginHistoryMetadata {

    private final String name;
    @Nullable
    private final String version;
    private final long modified;

    public PluginHistoryMetadata(String name, @Nullable String version, long modified) {
        this.name = name;
        this.version = version;
        this.modified = modified;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    public long getModified() {
        return modified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginHistoryMetadata that = (PluginHistoryMetadata) o;
        return getModified() == that.getModified() && Objects.equals(getName(), that.getName()) && Objects.equals(getVersion(), that.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getVersion(), getModified());
    }

    @Override
    public String toString() {
        return "PluginHistoryMetadata{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", modified=" + modified +
                '}';
    }
}
