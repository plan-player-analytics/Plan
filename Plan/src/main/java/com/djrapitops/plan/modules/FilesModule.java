package com.djrapitops.plan.modules;

import com.djrapitops.plan.system.file.PlanFiles;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

/**
 * Dagger Module for the Plan files.
 *
 * @author Rsl1122
 */
@Module
public class FilesModule {

    @Provides
    @Named("configFile")
    @Singleton
    File provideConfigFile(PlanFiles files) {
        return files.getConfigFile();
    }

}