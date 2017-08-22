/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.handling.player;

import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BanProcessor extends PlayerProcessor {

    private final boolean banned;

    public BanProcessor(UUID uuid, boolean banned) {
        super(uuid);
        this.banned = banned;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        // TODO DB Update Ban status
    }
}