package main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;

import java.util.UUID;

/**
 * HandlingInfo Class for QuitEvent information.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
@Deprecated //TODO Update straight to db
public class LogoutInfo extends HandlingInfo {

    private final boolean banned;
    private final PlaytimeDependentInfo playtimeDependentInfo;

    /**
     * Constructor.
     *
     * @param uuid   UUID of the player.
     * @param time   Epoch ms of the event.
     * @param banned Is the player banned
     * @param gm     current gamemode of the player
     */
    public LogoutInfo(UUID uuid, long time, boolean banned, String gm, String worldName) {
        super(uuid, InfoType.LOGOUT, time);
        this.banned = banned;
        this.playtimeDependentInfo = new PlaytimeDependentInfo(uuid, InfoType.OTHER, time, gm, worldName);
    }

    @Override
    public void process(UserData uData) {
    }

}
