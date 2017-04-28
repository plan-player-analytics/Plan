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

    /**
     *
     * @param uuid
     * @param type
     * @param time
     */
    public HandlingInfo(UUID uuid, InfoType type, long time) {
        this.uuid = uuid;
        this.type = type;
        this.time = time;
    }

    /**
     *
     * @return
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     *
     * @return
     */
    public InfoType getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public long getTime() {
        return time;
    }

    /**
     *
     * @param uData
     * @return
     */
    public abstract boolean process(UserData uData);
}
