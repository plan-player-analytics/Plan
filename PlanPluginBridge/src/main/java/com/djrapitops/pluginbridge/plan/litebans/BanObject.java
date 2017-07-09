package com.djrapitops.pluginbridge.plan.litebans;

import java.util.UUID;

/**
 * Class representing LiteBans database data about a ban.
 *
 * @author Rsl1122
 */
public class BanObject {
    private final UUID uuid;
    private final String reason;
    private final String bannedBy;
    private final long expires;

    public BanObject(UUID uuid, String reason, String bannedBy, long expires) {
        this.uuid = uuid;
        this.reason = reason;
        this.bannedBy = bannedBy;
        this.expires = expires;
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
        return expires;
    }
}
