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
package com.djrapitops.plan.settings.upkeep;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.network.NetworkSettingManager;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Task on networks that stores server configs in /plugins/Plan/serverConfiguration in database on boot.
 *
 * @author AuroraLS3
 */
@Singleton
public class NetworkConfigStoreTask extends TaskSystem.Task {

    private final NetworkSettingManager networkSettingManager;
    private final PlanConfig config;

    @Inject
    public NetworkConfigStoreTask(
            PlanConfig config,
            NetworkSettingManager networkSettingManager
    ) {
        this.config = config;
        this.networkSettingManager = networkSettingManager;
    }

    @Override
    public void run() {
        try {
            updateDBConfigs();
        } finally {
            cancel();
        }
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        long delay = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MILLISECONDS) + 40;

        runnableFactory.create(this).runTaskLaterAsynchronously(delay);
    }

    private void updateDBConfigs() {
        File[] configFiles = networkSettingManager.getConfigFiles();

        for (File configFile : configFiles) {
            ServerUUID serverUUID = NetworkSettingManager.getServerUUIDFromFilename(configFile);
            networkSettingManager.updateConfigInDB(configFile, serverUUID);
        }
    }
}