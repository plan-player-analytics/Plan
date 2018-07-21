package com.djrapitops.plan.data.container;

import java.util.Objects;
import java.util.UUID;

/**
 * Used for storing information of players after it has been fetched.
 *
 * @author Rsl1122
 */
public class UserInfo {

    private final UUID uuid;
    private String name;
    private long registered;
    private long lastSeen;
    private boolean banned;
    private boolean opped;

    public UserInfo(UUID uuid) {
        this.uuid = uuid;
    }

    public UserInfo(UUID uuid, String name, long registered, boolean opped, boolean banned) {
        this.uuid = uuid;
        this.name = name;
        this.registered = registered;
        this.opped = opped;
        this.banned = banned;
        lastSeen = 0L;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public long getRegistered() {
        return registered;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public boolean isBanned() {
        return banned;
    }

    public boolean isOperator() {
        return opped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return registered == userInfo.registered &&
                lastSeen == userInfo.lastSeen &&
                banned == userInfo.banned &&
                opped == userInfo.opped &&
                Objects.equals(uuid, userInfo.uuid) &&
                Objects.equals(name, userInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, registered, lastSeen, banned, opped);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", registered=" + registered +
                ", lastSeen=" + lastSeen +
                ", banned=" + banned +
                ", opped=" + opped +
                '}';
    }
}