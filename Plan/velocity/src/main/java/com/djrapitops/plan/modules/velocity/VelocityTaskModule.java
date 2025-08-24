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
package com.djrapitops.plan.modules.velocity;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.web.ResourceWriteTask;
import com.djrapitops.plan.delivery.webserver.auth.ActiveCookieExpiryCleanupTask;
import com.djrapitops.plan.delivery.webserver.cache.JSONFileStorage;
import com.djrapitops.plan.delivery.webserver.configuration.AddressAllowList;
import com.djrapitops.plan.extension.ExtensionServerDataUpdater;
import com.djrapitops.plan.gathering.timed.InstalledPluginGatheringTask;
import com.djrapitops.plan.gathering.timed.ProxyTPSCounter;
import com.djrapitops.plan.gathering.timed.SystemUsageBuffer;
import com.djrapitops.plan.gathering.timed.VelocityPingCounter;
import com.djrapitops.plan.settings.upkeep.NetworkConfigStoreTask;
import com.djrapitops.plan.storage.upkeep.DBCleanTask;
import com.djrapitops.plan.storage.upkeep.LogsFolderCleanTask;
import com.djrapitops.plan.storage.upkeep.OldDependencyCacheDeletionTask;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface VelocityTaskModule {

    @Binds
    @IntoSet
    TaskSystem.Task bindTPSCounter(ProxyTPSCounter counter);

    @Binds
    @IntoSet
    TaskSystem.Task bindPingCounter(VelocityPingCounter counter);

    @Binds
    @IntoSet
    TaskSystem.Task bindNetworkConfigStoreTask(NetworkConfigStoreTask configStoreTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindExtensionServerDataUpdater(ExtensionServerDataUpdater extensionServerDataUpdater);

    @Binds
    @IntoSet
    TaskSystem.Task bindLogCleanTask(LogsFolderCleanTask logsFolderCleanTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindDBCleanTask(DBCleanTask cleanTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindRamAndCpuTask(SystemUsageBuffer.RamAndCpuTask ramAndCpuTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindDiskTask(SystemUsageBuffer.DiskTask diskTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindJSONFileStorageCleanTask(JSONFileStorage.CleanTask cleanTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindOldDependencyCacheDeletion(OldDependencyCacheDeletionTask deletionTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindResourceWriteTask(ResourceWriteTask resourceWriteTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindActiveCookieStoreExpiryTask(ActiveCookieExpiryCleanupTask activeCookieExpiryCleanupTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindAddressAllowListUpdateTask(AddressAllowList addressAllowList);

    @Binds
    @IntoSet
    TaskSystem.Task bindInstalledPluginGatheringTask(InstalledPluginGatheringTask installedPluginGatheringTask);
}
