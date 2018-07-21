package com.djrapitops.plan.data.store.mutators;


import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.ServerKeys;

import java.util.HashMap;
import java.util.Map;

/**
 * Mutator for Command Usage Map objects.
 * <p>
 * Can be used to easily get different values about the map.
 *
 * @author Rsl1122
 */
public class CommandUseMutator {

    private Map<String, Integer> commandUsage;

    public CommandUseMutator(Map<String, Integer> commandUsage) {
        this.commandUsage = commandUsage;
    }

    public static CommandUseMutator forContainer(DataContainer container) {
        return new CommandUseMutator(container.getValue(ServerKeys.COMMAND_USAGE).orElse(new HashMap<>()));
    }

    public int commandUsageCount() {
        int total = 0;
        for (Integer value : commandUsage.values()) {
            total += value;
        }
        return total;
    }
}