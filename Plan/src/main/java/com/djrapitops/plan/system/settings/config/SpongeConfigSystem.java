package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.IOException;

/**
 * Sponge ConfigSystem that disables WebServer and Geolocations on first enable.
 *
 * @author Rsl1122
 */
public class SpongeConfigSystem extends ServerConfigSystem {

    private boolean firstInstall;

    @Override
    public void enable() throws EnableException {
        firstInstall = !FileSystem.getConfigFile().exists();
        super.enable();
    }

    @Override
    protected void copyDefaults() throws IOException {
        super.copyDefaults();
        if (firstInstall) {
            Log.info("WebServer and Geolocations disabled by default. Please enable them in the config.");
            Settings.WEBSERVER_DISABLED.set(true);
            Settings.DATA_GEOLOCATIONS.set(false);
            Settings.save();
        }
    }
}