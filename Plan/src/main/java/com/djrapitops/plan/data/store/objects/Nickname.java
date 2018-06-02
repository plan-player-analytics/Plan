package com.djrapitops.plan.data.store.objects;

import java.util.Objects;
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

    @Override
    public String toString() {
        return "Nickname{" +
                "name='" + name + '\'' +
                ", lastUsed=" + lastUsed +
                ", serverUUID=" + serverUUID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nickname)) return false;
        Nickname nickname = (Nickname) o;
        return lastUsed == nickname.lastUsed &&
                Objects.equals(name, nickname.name) &&
                Objects.equals(serverUUID, nickname.serverUUID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, lastUsed, serverUUID);
    }
}