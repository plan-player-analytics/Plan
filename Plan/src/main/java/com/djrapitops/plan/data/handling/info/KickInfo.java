package main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;

import java.util.UUID;

/**
 * HandlingInfo Class for KickEvent information.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
@Deprecated //TODO Update Straight to db
public class KickInfo extends HandlingInfo {

    /**
     * Constructor.
     *
     * @param uuid UUID of the kicked player.
     */
    public KickInfo(UUID uuid) {
        super(uuid, InfoType.KICK, 0L);
    }

    @Override
    public void process(UserData uData) {
    }

}
