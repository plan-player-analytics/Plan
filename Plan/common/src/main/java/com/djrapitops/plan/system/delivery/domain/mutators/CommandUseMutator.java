/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.delivery.domain.mutators;

import com.djrapitops.plan.system.delivery.domain.container.DataContainer;
import com.djrapitops.plan.system.delivery.domain.keys.ServerKeys;

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