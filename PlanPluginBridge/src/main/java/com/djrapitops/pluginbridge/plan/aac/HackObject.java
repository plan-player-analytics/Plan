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
package com.djrapitops.pluginbridge.plan.aac;

import java.util.UUID;

/**
 * Data object for AAC data.
 *
 * @author Rsl1122
 */
public class HackObject {

    private final UUID uuid;
    private final long date;
    private final String hackType;
    private final int violationLevel;

    public HackObject(UUID uuid, long date, String hackType, int violationLevel) {
        this.uuid = uuid;
        this.date = date;
        this.hackType = hackType;
        this.violationLevel = violationLevel;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getDate() {
        return date;
    }

    public String getHackType() {
        return hackType;
    }

    public int getViolationLevel() {
        return violationLevel;
    }
}