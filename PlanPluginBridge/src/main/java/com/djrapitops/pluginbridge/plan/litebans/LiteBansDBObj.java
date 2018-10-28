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
package com.djrapitops.pluginbridge.plan.litebans;

import java.util.UUID;

/**
 * Class representing LiteBans database data about a ban.
 *
 * @author Rsl1122
 */
public class LiteBansDBObj {
    private final UUID uuid;
    private final String reason;
    private final String bannedBy;
    private final long expiry;
    private final boolean active;
    private final long time;

    public LiteBansDBObj(UUID uuid, String reason, String bannedBy, long expiry, boolean active, long time) {
        this.uuid = uuid;
        this.reason = reason;
        this.bannedBy = bannedBy;
        this.expiry = expiry;
        this.active = active;
        this.time = time;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getReason() {
        return reason;
    }

    public String getBannedBy() {
        return bannedBy;
    }

    public long getExpiry() {
        return expiry;
    }

    public boolean isActive() {
        return active;
    }

    public long getTime() {
        return time;
    }
}
