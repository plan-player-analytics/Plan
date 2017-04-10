package main.java.com.djrapitops.plan.data.handling.info;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;

/**
 *
 * @author Rsl1122
 */
public abstract class HandlingInfo {

    UUID uuid;
    InfoType type;
    long time;

    public HandlingInfo(UUID uuid, InfoType type, long time) {
        this.uuid = uuid;
        this.type = type;
        this.time = time;
    }

    public UUID getUuid() {
        return uuid;
    }

    public InfoType getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    public abstract boolean process(UserData uData);
}
