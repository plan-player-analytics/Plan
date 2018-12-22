package com.djrapitops.plan.api.data;

import com.djrapitops.plan.data.store.Key;

import java.util.Optional;

/**
 * Wrapper for a ServerContainer.
 * <p>
 * The actual object is wrapped to avoid exposing too much API that might change.
 * See {@link com.djrapitops.plan.data.store.keys.ServerKeys} for Key objects.
 * <p>
 * The Keys might change in the future, but the Optional API should help dealing with those cases.
 *
 * @author Rsl1122
 */
public class ServerContainer {

    private final com.djrapitops.plan.data.store.containers.ServerContainer container;

    public ServerContainer(com.djrapitops.plan.data.store.containers.ServerContainer container) {
        this.container = container;
    }

    public <T> Optional<T> getValue(Key<T> key) {
        return container.getValue(key);
    }
}
