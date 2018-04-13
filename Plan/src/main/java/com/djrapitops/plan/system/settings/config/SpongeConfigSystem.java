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
            Log.info("§eWebServer and Geolocations disabled by default on Sponge servers. You can enable them in the config:");
            Log.info("§e  " + Settings.WEBSERVER_DISABLED.getPath());
            Log.info("§e  " + Settings.DATA_GEOLOCATIONS.getPath());
            Settings.WEBSERVER_DISABLED.set(true);
            Settings.DATA_GEOLOCATIONS.set(false);
            Settings.save();
        }
    }
}