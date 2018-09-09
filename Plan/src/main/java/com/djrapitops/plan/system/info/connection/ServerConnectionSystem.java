/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.*;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Connection system for Bukkit servers.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerConnectionSystem extends ConnectionSystem {

    private final Locale locale;
    private final PlanConfig config;
    private final Processing processing;
    private final Database database;
    private final Lazy<WebServer> webServer;
    private final PluginLogger pluginLogger;
    private final WebExceptionLogger webExceptionLogger;

    private long latestServerMapRefresh;

    private Server mainServer;

    @Inject
    public ServerConnectionSystem(
            Locale locale,
            PlanConfig config,
            Processing processing,
            Database database,
            Lazy<WebServer> webServer,
            ConnectionLog connectionLog,
            InfoRequests infoRequests,
            Lazy<InfoSystem> infoSystem,
            ServerInfo serverInfo,
            PluginLogger pluginLogger,
            WebExceptionLogger webExceptionLogger
    ) {
        super(connectionLog, infoRequests, infoSystem, serverInfo);
        this.locale = locale;
        this.config = config;
        this.processing = processing;
        this.database = database;
        this.webServer = webServer;
        this.pluginLogger = pluginLogger;
        this.webExceptionLogger = webExceptionLogger;
        latestServerMapRefresh = 0;
    }

    private void refreshServerMap() {
        processing.submitNonCritical(() -> {
            if (latestServerMapRefresh < System.currentTimeMillis() - TimeAmount.SECOND.ms() * 15L) {
                Optional<Server> bungeeInformation = database.fetch().getBungeeInformation();
                bungeeInformation.ifPresent(server -> mainServer = server);
                bukkitServers = database.fetch().getBukkitServers();
                latestServerMapRefresh = System.currentTimeMillis();
            }
        });
    }

    @Override
    protected Server selectServerForRequest(InfoRequest infoRequest) throws NoServersException {
        refreshServerMap();

        if (mainServer == null && bukkitServers.isEmpty()) {
            throw new NoServersException("Zero servers available to process requests.");
        }

        Server server = null;
        if (infoRequest instanceof CacheRequest ||
                infoRequest instanceof GenerateInspectPageRequest) {
            server = mainServer;
        } else if (infoRequest instanceof GenerateAnalysisPageRequest) {
            UUID serverUUID = ((GenerateAnalysisPageRequest) infoRequest).getServerUUID();
            server = bukkitServers.get(serverUUID);
        }
        if (server == null) {
            throw new NoServersException("Proper server is not available to process request: " + infoRequest.getClass().getSimpleName());
        }
        return server;
    }

    @Override
    public void sendWideInfoRequest(WideRequest infoRequest) throws NoServersException {
        if (bukkitServers.isEmpty()) {
            throw new NoServersException("No Servers available to make wide-request: " + infoRequest.getClass().getSimpleName());
        }
        for (Server server : bukkitServers.values()) {
            webExceptionLogger.logIfOccurs(this.getClass(), () -> {
                try {
                    sendInfoRequest(infoRequest, server);
                } catch (ConnectionFailException ignored) {
                    /* Wide Requests are used when at least one result is wanted. */
                }
            });
        }
    }

    @Override
    public boolean isServerAvailable() {
        return mainServer != null && config.isFalse(Settings.BUNGEE_OVERRIDE_STANDALONE_MODE);
    }

    @Override
    public String getMainAddress() {
        return isServerAvailable() ? mainServer.getWebAddress() : serverInfo.getServer().getWebAddress();

    }

    @Override
    public void enable() {
        super.enable();
        refreshServerMap();

        boolean usingBungeeWebServer = isServerAvailable();
        boolean usingAlternativeIP = config.isTrue(Settings.SHOW_ALTERNATIVE_IP);

        if (!usingAlternativeIP && serverInfo.getServerProperties().getIp().isEmpty()) {
            pluginLogger.log(L.INFO_COLOR, "§e" + locale.getString(PluginLang.ENABLE_NOTIFY_EMPTY_IP));
        }
        if (usingBungeeWebServer && usingAlternativeIP) {
            String webServerAddress = webServer.get().getAccessAddress();
            pluginLogger.info(locale.getString(PluginLang.ENABLE_NOTIFY_ADDRESS_CONFIRMATION, webServerAddress));
        }
    }
}
