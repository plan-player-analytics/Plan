/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.handling.player;

import main.java.com.djrapitops.plan.Plan;

import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class RegisterProcessor extends PlayerProcessor {

    private final long time;
    private final int playersOnline;

    public RegisterProcessor(UUID uuid, long time, int playersOnline) {
        super(uuid);
        this.time = time;
        this.playersOnline = playersOnline;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        if (Plan.getInstance().getDB().wasSeenBefore(uuid)) {
            return;
        }
        // TODO DB Register
    }
}