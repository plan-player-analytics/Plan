package main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.WorldTimeHandling;

import java.util.UUID;

/**
 * HandlingInfo Class for PlayerChangedWorldEvent information.
 *
 * @author Rsl1122
 * @since 3.6.0
 */
public class WorldInfo extends HandlingInfo {

    private final String currentWorld;

    /**
     * Constructor.
     *
     * @param uuid         UUID of the player related to the info.
     * @param time         Time the event occurred
     * @param currentWorld World the player is currently in.
     */
    public WorldInfo(UUID uuid, long time, String currentWorld) {
        super(uuid, InfoType.WORLD, time);
        this.currentWorld = currentWorld;
    }

    @Override
    public boolean process(UserData uData) {
        if (!uData.getUuid().equals(uuid)) {
            return false;
        }
        WorldTimeHandling.processWorldChangeInfo(uData, time, currentWorld);
        return true;
    }
}
