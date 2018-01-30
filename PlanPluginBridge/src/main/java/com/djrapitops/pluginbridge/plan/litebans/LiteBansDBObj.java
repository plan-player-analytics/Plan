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

    public LiteBansDBObj(UUID uuid, String reason, String bannedBy, long expiry, boolean active) {
        this.uuid = uuid;
        this.reason = reason;
        this.bannedBy = bannedBy;
        this.expiry = expiry;
        this.active = active;
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
}
