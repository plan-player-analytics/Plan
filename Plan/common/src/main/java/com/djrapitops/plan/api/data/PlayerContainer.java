package com.djrapitops.plan.api.data;

import com.djrapitops.plan.data.store.Key;

import java.util.Optional;

/**
 * Wrapper for a PlayerContainer.
 * <p>
 * The actual object is wrapped to avoid exposing too much API that might change.
 * See {@link com.djrapitops.plan.data.store.keys.PlayerKeys} for Key objects.
 * <p>
 * The Keys might change in the future, but the Optional API should help dealing with those cases.
 *
 * @author Rsl1122
 */
public class PlayerContainer {

    private final com.djrapitops.plan.data.store.containers.PlayerContainer container;

    public PlayerContainer(com.djrapitops.plan.data.store.containers.PlayerContainer container) {
        this.container = container;
    }

    public double getActivityIndex(long date, long playtimeMsThreshold, int loginThreshold) {
        return container.getActivityIndex(date, playtimeMsThreshold, loginThreshold).getValue();
    }

    public boolean playedBetween(long after, long before) {
        return container.playedBetween(after, before);
    }

    public <T> Optional<T> getValue(Key<T> key) {
        return container.getValue(key);
    }
}
