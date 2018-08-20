package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Sponge ConfigSystem that disables WebServer and Geolocations on first enable.
 *
 * @author Rsl1122
 */
public class SpongeConfigSystem extends ServerConfigSystem {

    private final PluginLogger logger;

    private boolean firstInstall;

    @Inject
    public SpongeConfigSystem(FileSystem fileSystem, PluginLogger logger) {
        super(fileSystem);
        this.logger = logger;
    }

    @Override
    public void enable() throws EnableException {
        firstInstall = !fileSystem.getConfigFile().exists();
        super.enable();
    }

    @Override
    protected void copyDefaults() throws IOException {
        super.copyDefaults();
        if (firstInstall) {
            logger.info("§eWebServer and Geolocations disabled by default on Sponge servers. You can enable them in the config:");
            logger.info("§e  " + Settings.WEBSERVER_DISABLED.getPath());
            logger.info("§e  " + Settings.DATA_GEOLOCATIONS.getPath());

            config.set(Settings.WEBSERVER_DISABLED, true);
            config.set(Settings.DATA_GEOLOCATIONS, false);
            config.save();
        }
    }
}