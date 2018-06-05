package com.djrapitops.plan.data.store.containers;

import java.util.HashMap;
import java.util.UUID;

/**
 * Container for data about a player linked to a single server.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.keys.PerServerKeys For Key objects.
 */
public class PerServerContainer extends HashMap<UUID, DataContainer> {
}