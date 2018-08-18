package com.djrapitops.plan.modules.common;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.file.FileSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for providing FileSystem.
 *
 * @author Rsl1122
 */
@Module
public class FileSystemModule {

    @Singleton
    @Provides
    FileSystem provideFileSystem(PlanPlugin plugin) {
        return new FileSystem(plugin.getDataFolder());
    }

}