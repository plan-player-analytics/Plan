package main.java.com.djrapitops.plan.data;

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
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRegistered() {
        return registered;
    }

    public void setRegistered(long registered) {
        this.registered = registered;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean isBanned() {
        return banned;
    }

    public boolean isOpped() {
        return opped;
    }
}