package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.data.store.mutators.SessionsMutator;

/**
 * DataContainer about a Player.
 * <p>
 * Use {@code getValue(PlayerKeys.REGISTERED).isPresent()} to determine if Plan has data about the player.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.keys.PlayerKeys For Key objects.
 */
public class PlayerContainer extends DataContainer {

    public boolean playedBetween(long after, long before) {
        return SessionsMutator.forContainer(this).playedBetween(after, before);
    }

}