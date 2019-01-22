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
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.sql.queries.LargeFetchQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.*;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private final DBSystem dbSystem;
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
            DBSystem dbSystem,
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
        this.dbSystem = dbSystem;
        this.webServer = webServer;
        this.pluginLogger = pluginLogger;
        this.webExceptionLogger = webExceptionLogger;
        latestServerMapRefresh = 0;
    }

    private void refreshServerMap() {
        processing.submitNonCritical(() -> {
            if (latestServerMapRefresh < System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(15L)) {
                Database database = dbSystem.getDatabase();
                Map<UUID, Server> servers = database.query(LargeFetchQueries.fetchPlanServerInformation());
                Optional<Server> proxy = servers.values().stream()
                        .filter(Server::isProxy)
                        .findFirst();
                mainServer = proxy.orElse(null);

                proxy.ifPresent(proxyServer -> servers.remove(proxyServer.getUuid()));

                dataServers = servers;
                latestServerMapRefresh = System.currentTimeMillis();
            }
        });
    }

    @Override
    protected Server selectServerForRequest(InfoRequest infoRequest) throws NoServersException {
        refreshServerMap();

        if (mainServer == null && dataServers.isEmpty()) {
            throw new NoServersException("Zero servers available to process requests.");
        }

        Server server = null;
        if (infoRequest instanceof CacheRequest ||
                infoRequest instanceof GenerateInspectPageRequest) {
            server = mainServer;
        } else if (infoRequest instanceof GenerateAnalysisPageRequest) {
            UUID serverUUID = ((GenerateAnalysisPageRequest) infoRequest).getServerUUID();
            server = dataServers.get(serverUUID);
        }
        if (server == null) {
            throw new NoServersException("Proper server is not available to process request: " + infoRequest.getClass().getSimpleName());
        }
        return server;
    }

    @Override
    public void sendWideInfoRequest(WideRequest infoRequest) throws NoServersException {
        if (dataServers.isEmpty()) {
            throw new NoServersException("No Servers available to make wide-request: " + infoRequest.getClass().getSimpleName());
        }
        for (Server server : dataServers.values()) {
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
        return mainServer != null;
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
        boolean usingAlternativeIP = config.isTrue(WebserverSettings.SHOW_ALTERNATIVE_IP);

        if (!usingAlternativeIP && serverInfo.getServerProperties().getIp().isEmpty()) {
            pluginLogger.log(L.INFO_COLOR, "§e" + locale.getString(PluginLang.ENABLE_NOTIFY_EMPTY_IP));
        }
        if (usingBungeeWebServer && usingAlternativeIP) {
            String webServerAddress = webServer.get().getAccessAddress();
            pluginLogger.info(locale.getString(PluginLang.ENABLE_NOTIFY_ADDRESS_CONFIRMATION, webServerAddress));
        }
    }
}
