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
package net.playeranalytics.plan;


import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.*;
import com.djrapitops.plan.settings.config.paths.key.Setting;
import net.playeranalytics.plugin.StandalonePlatformAbstractionLayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class PlanStandalone implements PlanPlugin {

    private static final Logger LOGGER = Logger.getGlobal();

    private static final ScannerPrompter SCANNER_PROMPTER = new ScannerPrompter();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private static PlanStandalone pluginInstance;

    private PlanSystem system;

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(PlanStandalone.class.getResourceAsStream("/logging.properties"));

        LOGGER.info("Starting Plan..");
        LOGGER.info("Type 'exit' at any time to stop the program.");
        LOGGER.info(() -> "Java version: " + System.getProperty("java.version"));
        LOGGER.info("");
        pluginInstance = new PlanStandalone();
        EXECUTOR_SERVICE.submit(pluginInstance::onEnable);

        // Blocks and waits user input to the console
        SCANNER_PROMPTER.enable();
    }

    public static void shutdown(int status) {
        LOGGER.info("Stopping the program...");
        if (pluginInstance != null) pluginInstance.onDisable();
        SCANNER_PROMPTER.disable();
        EXECUTOR_SERVICE.shutdown();
        try {
            if (!EXECUTOR_SERVICE.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR_SERVICE.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.info("Press enter to exit..");
            System.exit(131);
            Thread.currentThread().interrupt();
            return;
        }
        if (status != 0) LOGGER.info("Press enter to exit..");
        System.exit(status);
    }

    @Override
    public InputStream getResource(String resource) {
        try {
            return pluginInstance.getSystem().getPlanFiles().getResourceFromJar(resource).asInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ColorScheme getColorScheme() {
        return new ColorScheme("", "", "");
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }

    @Override
    public void registerCommand(Subcommand command) {
        // no-op, unused
    }

    @Override
    public void onEnable() {
        try {
            PlanStandaloneComponent component = DaggerPlanStandaloneComponent.builder()
                    .plan(this)
                    .abstractionLayer(new StandalonePlatformAbstractionLayer(LOGGER))
                    .build();

            system = component.system();
            system.enableForCommands();

            PlanConfig config = system.getConfigSystem().getConfig();

            config.set(WebserverSettings.DISABLED, false);
            config.set(DataGatheringSettings.GEOLOCATIONS, false);
            config.set(DataGatheringSettings.DISK_SPACE, false);
            config.set(DataGatheringSettings.PING, false);

            String ip = config.get(ProxySettings.IP);
            if ("0.0.0.0".equals(ip)) {
                // First installation, prompt for settings
                LOGGER.info("\n--------------\n");
                promptSetting(config, ProxySettings.IP, "Please enter IP / address to access this server");
                promptSetting(config, DatabaseSettings.MYSQL_HOST, "Please enter MySQL address");
                promptSetting(config, DatabaseSettings.MYSQL_PORT, "Please enter MySQL port");
                promptSetting(config, DatabaseSettings.MYSQL_DATABASE, "Please enter MySQL database name/schema name");
                promptSetting(config, DatabaseSettings.MYSQL_USER, "Please enter MySQL user");
                promptSetting(config, DatabaseSettings.MYSQL_PASS, "Please enter MySQL password");
                promptSettingInt(config, WebserverSettings.PORT, "Please enter Webserver port to use");
                config.set(PluginSettings.SERVER_NAME, "Standalone Plan Instance");
                LOGGER.info("Saving config..");
                config.save();
                LOGGER.info("\n--------------\n");
                LOGGER.info("Config saved - proceeding with plugin enable..");
            }

            SCANNER_PROMPTER.insertCommands(component.planCommand());

        } catch (Exception | Error e) {
            LOGGER.log(Level.SEVERE, "Failed to enable commands" + e + ", program will exit\n", e);
            shutdown(1);
        }
        try {
            system.enable();
            LOGGER.info("-- Startup complete, Plan enabled successfully!");
        } catch (Exception | Error e) {
            LOGGER.log(Level.SEVERE, "Failed to enable plugin " + e + ", you can try 'plan reload' after changing config settings.\n", e);
        }
    }

    private void promptSetting(PlanConfig config, Setting<String> setting, String prompt) {
        LOGGER.info(() -> prompt + " (" + setting.getPath() + " setting):");
        String inputValue = SCANNER_PROMPTER.waitAndGetInput()
                .orElseThrow(() -> new IllegalStateException("KeyboardInterrupt"));
        config.set(setting, inputValue);
        LOGGER.info(() -> "Set '" + setting.getPath() + "' as '" + inputValue + "'");
    }

    private void promptSettingInt(PlanConfig config, Setting<Integer> setting, String prompt) {
        LOGGER.info(() -> prompt + " (" + setting.getPath() + " setting):");
        String inputValue = SCANNER_PROMPTER.waitAndGetInput()
                .orElseThrow(() -> new IllegalStateException("KeyboardInterrupt"));

        try {
            config.set(setting, Integer.parseInt(inputValue));
        } catch (NumberFormatException invalid) {
            LOGGER.warning("'" + inputValue + "' is not a valid number, try again");
            promptSettingInt(config, setting, prompt);
            return;
        }
        LOGGER.info(() -> "Set '" + setting.getPath() + "' as '" + inputValue + "'");
    }

    @Override
    public void onDisable() {
        if (system != null) system.disable();
    }

    @Override
    public File getDataFolder() {
        return pluginInstance.getSystem().getPlanFiles().getDataFolder();
    }
}
