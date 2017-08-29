package main.java.com.djrapitops.plan.systems.processing.player;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Session;

import java.util.Optional;
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
        Optional<Session> cachedSession = Plan.getInstance().getDataCache().getCachedSession(uuid);
        cachedSession.ifPresent(Session::died);
    }
}
