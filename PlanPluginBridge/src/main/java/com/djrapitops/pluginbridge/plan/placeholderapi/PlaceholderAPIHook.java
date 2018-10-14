package com.djrapitops.pluginbridge.plan.placeholderapi;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import me.clip.placeholderapi.PlaceholderAPI;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A Class responsible for hooking to PlaceholderAPI.
 *
 * @author Rsl1122
 */
@Singleton
public class PlaceholderAPIHook extends Hook {

    private final PlanPlugin plugin;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final WebServer webServer;
    private final Formatters formatters;
    private final ErrorHandler errorHandler;

    @Inject
    public PlaceholderAPIHook(
            PlanPlugin plugin,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            WebServer webServer,
            Formatters formatters,
            ErrorHandler errorHandler
    ) {
        super("me.clip.placeholderapi.PlaceholderAPI");

        this.plugin = plugin;
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.webServer = webServer;
        this.formatters = formatters;
        this.errorHandler = errorHandler;
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            PlaceholderAPI.unregisterPlaceholderHook("plan");
            PlaceholderAPI.registerPlaceholderHook("plan",
                    new PlanPlaceholders(plugin, config, dbSystem, serverInfo, webServer, formatters, errorHandler)
            );
        }
    }
}
