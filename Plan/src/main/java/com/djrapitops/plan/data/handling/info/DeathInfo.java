package main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;

import java.util.UUID;

/**
 * HandlingInfo Class for DeathEvent information.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class DeathInfo extends HandlingInfo {

    /**
     * Constructor.
     *
     * @param uuid UUID of the dead player.
     */
    public DeathInfo(UUID uuid) {
        super(uuid, InfoType.DEATH, 0L);
    }

    @Override
    public void process(UserData uData) {
        if (!uData.getUuid().equals(uuid)) {
            return;
        }
       //TODO uData.setDeaths(uData.getDeaths() + 1);
    }
}
