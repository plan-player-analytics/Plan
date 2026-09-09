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
package com.djrapitops.plan.storage.database.queries.objects;

/**
 * Object for holding total playtime and session count.
 *
 * @author AuroraLS3
 */
public class PlaytimeAndCount {

    private final long playtime;
    private final long count;

    public PlaytimeAndCount(long playtime, long count) {
        this.playtime = playtime;
        this.count = count;
    }

    public long getPlaytime() {
        return playtime;
    }

    public long getCount() {
        return count;
    }
}
