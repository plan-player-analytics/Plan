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
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.utilities.java.Lists;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds reference to all player specific gathering tasks that should be cancelled on player logout.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlayerGatheringTasks {

    private final Map<UUID, List<TaskSystem.Task>> tasks;

    public PlayerGatheringTasks() {
        tasks = new ConcurrentHashMap<>();
    }

    public void register(UUID playerUUID, TaskSystem.Task task) {
        tasks.computeIfAbsent(playerUUID, Lists::create).add(task);
    }

    public void unregister(UUID playerUUID) {
        List<TaskSystem.Task> registered = tasks.get(playerUUID);
        tasks.remove(playerUUID);
        if (registered != null) {
            registered.forEach(TaskSystem.Task::cancel);
            registered.clear();
        }
    }
}
