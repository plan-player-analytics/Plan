/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.player;

import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class OPProcessor extends PlayerProcessor {

    private final boolean banned;

    public OPProcessor(UUID uuid, boolean banned) {
        super(uuid);
        this.banned = banned;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        // TODO DB Update Ban status
    }
}