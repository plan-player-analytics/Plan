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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.gathering.PlayerGatheringTasks;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * Updates player values periodically.
 *
 * @author AuroraLS3
 */
public class PlayerExtensionDataUpdateTask extends TaskSystem.Task {

    private final PlanConfig config;
    private final PlayerGatheringTasks playerGatheringTasks;
    private final ExtensionSvc extensionService;
    private final Parameters.PlayerParameters parameters;

    public PlayerExtensionDataUpdateTask(PlanConfig config, PlayerGatheringTasks playerGatheringTasks, ExtensionSvc extensionService, Parameters.PlayerParameters parameters) {
        this.config = config;
        this.playerGatheringTasks = playerGatheringTasks;
        this.extensionService = extensionService;
        this.parameters = parameters;
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        Long refreshPeriod = config.get(TimeSettings.EXTENSION_PLAYER_DATA_REFRESH_PERIOD);
        runnableFactory.create(this)
                .runTaskTimerAsynchronously(refreshPeriod, refreshPeriod, TimeUnit.MILLISECONDS);
        playerGatheringTasks.register(parameters.getPlayerUUID(), this);
    }

    @Override
    public void run() {
        extensionService.updatePlayerValues(parameters.getPlayerUUID(), parameters.getPlayerName(), CallEvents.PLAYER_PERIODICAL);
    }

    @Singleton
    public static class Factory {
        private final PlanConfig config;
        private final PlayerGatheringTasks playerGatheringTasks;
        private final RunnableFactory runnableFactory;
        private final ExtensionSvc extensionService;

        @Inject
        public Factory(PlanConfig config, PlayerGatheringTasks playerGatheringTasks, RunnableFactory runnableFactory, ExtensionSvc extensionService) {
            this.config = config;
            this.playerGatheringTasks = playerGatheringTasks;
            this.runnableFactory = runnableFactory;
            this.extensionService = extensionService;
        }

        public void register(Parameters playerParameters) {
            new PlayerExtensionDataUpdateTask(config, playerGatheringTasks, extensionService, (Parameters.PlayerParameters) playerParameters)
                    .register(runnableFactory);
        }
    }

}
