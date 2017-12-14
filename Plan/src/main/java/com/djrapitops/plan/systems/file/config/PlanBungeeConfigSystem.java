/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.file.config;

import main.java.com.djrapitops.plan.systems.file.FileSystem;

import java.io.IOException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PlanBungeeConfigSystem extends ConfigSystem {

    @Override
    protected void copyDefaults() throws IOException {
        config.copyDefaults(FileSystem.readFromResource("bungeeconfig.yml"));
    }
}