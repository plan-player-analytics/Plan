package main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;

import java.util.UUID;

/**
 * An abstract class for processing information about events and modifying
 * UserData objects associated with the events.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public abstract class HandlingInfo {

    final UUID uuid;
    final InfoType type;
    final long time;

    /**
     * Super Constructor.
     *
     * @param uuid UUID of the player
     * @param type InfoType enum of the event. Only used for debugging different
     * types.
     * @param time Epoch ms of the event.
     */
    public HandlingInfo(UUID uuid, InfoType type, long time) {
        this.uuid = uuid;
        this.type = type;
        this.time = time;
    }

    /**
     * Get the UUID.
     *
     * @return UUID of the player associated with the event.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the InfoType.
     *
     * @return InfoType enum.
     */
    public InfoType getType() {
        return type;
    }

    /**
     * Get the epoch ms the event occurred.
     *
     * @return long in ms.
     */
    public long getTime() {
        return time;
    }

    /**
     * Process the info and modify the UserData object accordingly.
     *
     * If the UUIDs don't match no change should occur.
     *
     * @param uData UserData object to modify.
     * @return UUID of the UserData object and HandlingInfo match.
     */
    public abstract boolean process(UserData uData);
}
