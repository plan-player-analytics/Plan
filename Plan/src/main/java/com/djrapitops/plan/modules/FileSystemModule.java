package com.djrapitops.plan.modules;

import com.djrapitops.plan.system.file.FileSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

/**
 * Dagger Module for the Plan FileSystem.
 *
 * @author Rsl1122
 */
@Module
public class FileSystemModule {

    @Provides
    @Named("configFile")
    @Singleton
    File provideConfigFile(FileSystem fileSystem) {
        return fileSystem.getConfigFile();
    }

}