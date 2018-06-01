package com.djrapitops.plan.data.store.keys;

import com.djrapitops.plan.data.store.Key;

import java.util.UUID;

/**
 * Class holding Key objects that are commonly used across multiple DataContainers.
 *
 * @author Rsl1122
 */
public class CommonKeys {

    public static final Key<UUID> UUID = new Key<>(UUID.class, "uuid");

}