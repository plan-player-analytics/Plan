package com.djrapitops.plan.data.store.objects;

import java.util.UUID;

/**
 * Object storing nickname information.
 *
 * @author Rsl1122
 */
public class Nickname {

    private final String name;
    private final long lastUsed;
    private final UUID serverUUID;

    public Nickname(String name, long lastUsed, UUID serverUUID) {
        this.name = name;
        this.lastUsed = lastUsed;
        this.serverUUID = serverUUID;
    }

    public String getName() {
        return name;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public UUID getServerUUID() {
        return serverUUID;
    }
}