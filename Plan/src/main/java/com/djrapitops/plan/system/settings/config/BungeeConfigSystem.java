/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.system.file.FileSystem;

import java.io.File;
import java.io.IOException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BungeeConfigSystem extends ConfigSystem {

    public BungeeConfigSystem(File configFile) {
        super(configFile);
    }

    @Override
    protected void copyDefaults() throws IOException {
        config.copyDefaults(FileSystem.readFromResource("bungeeconfig.yml"));
    }
}