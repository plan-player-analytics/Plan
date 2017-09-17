package main.java.com.djrapitops.plan.command;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;

import java.util.UUID;

/**
 * This class contains methods used by commands
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class ConditionUtils {

    /**
     * Constructor used to hide the public constructor
     */
    private ConditionUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if the player has played.
     *
     * @param uuid UUID of player
     * @return has the player played before, false if uuid is null.
     */
    public static boolean playerHasPlayed(UUID uuid) {
        return Verify.notNull(uuid) && Plan.getInstance().fetch().hasPlayedBefore(uuid);
    }
}
