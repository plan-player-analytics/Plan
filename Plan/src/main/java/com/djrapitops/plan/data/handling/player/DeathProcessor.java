package main.java.com.djrapitops.plan.data.handling.player;

import java.util.UUID;

/**
 * // TODO Write Javadoc Class Description
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class DeathProcessor extends PlayerProcessor {

    /**
     * Constructor.
     *
     * @param uuid UUID of the dead player.
     */
    public DeathProcessor(UUID uuid) {
        super(uuid);
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        // TODO DB Update Deaths +1
    }
}
