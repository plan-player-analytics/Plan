package com.djrapitops.plan.command;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.utilities.Verify;

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
     * Condition if the player has played.
     *
     * @param uuid UUID of player
     * @return has the player played before, false if uuid is null.
     */
    public static boolean playerHasPlayed(UUID uuid) {
        if (Verify.containsNull(uuid)) {
            return false;
        }
        boolean hasPlayed;
        if (Check.isBukkitAvailable()) {
            hasPlayed = Plan.getInstance().getServer().getOfflinePlayer(uuid).hasPlayedBefore();
        } else {
            hasPlayed = PlanBungee.getInstance().getDB().wasSeenBefore(uuid);
        }
        return hasPlayed;
    }
}
