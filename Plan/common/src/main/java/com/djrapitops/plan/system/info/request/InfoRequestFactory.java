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
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.export.HtmlExport;
import com.djrapitops.plan.system.export.JSONExport;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;
import com.djrapitops.plan.utilities.html.pages.PageFactory;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Factory for {@link InfoRequest} objects used for server-server communications.
 *
 * @author Rsl1122
 */
@Singleton
public class InfoRequestFactory {

    private final Lazy<PlanPlugin> plugin;
    private final Lazy<PlanConfig> config;
    private final Lazy<Processing> processing;
    private final Lazy<InfoSystem> infoSystem;
    private final Lazy<ConnectionSystem> connectionSystem;
    private final Lazy<WebExceptionLogger> webExceptionLogger;
    private final Lazy<ServerInfo> serverInfo;
    private final Lazy<ResponseFactory> responseFactory;
    private final Lazy<PageFactory> pageFactory;
    private final Lazy<HtmlExport> htmlExport;
    private final Lazy<JSONExport> jsonExport;
    private final Lazy<PluginLogger> logger;
    private final Lazy<RunnableFactory> runnableFactory;

    @Inject
    public InfoRequestFactory(
            Lazy<PlanPlugin> plugin,
            Lazy<PlanConfig> config,
            Lazy<Processing> processing,
            Lazy<InfoSystem> infoSystem,
            Lazy<ConnectionSystem> connectionSystem,
            Lazy<WebExceptionLogger> webExceptionLogger,
            Lazy<ServerInfo> serverInfo,
            Lazy<ResponseFactory> responseFactory,
            Lazy<PageFactory> pageFactory,
            Lazy<HtmlExport> htmlExport,
            Lazy<JSONExport> jsonExport,
            Lazy<PluginLogger> logger,
            Lazy<RunnableFactory> runnableFactory
    ) {
        this.plugin = plugin;
        this.config = config;
        this.processing = processing;
        this.infoSystem = infoSystem;
        this.connectionSystem = connectionSystem;
        this.webExceptionLogger = webExceptionLogger;
        this.serverInfo = serverInfo;
        this.responseFactory = responseFactory;
        this.pageFactory = pageFactory;
        this.htmlExport = htmlExport;
        this.jsonExport = jsonExport;
        this.logger = logger;
        this.runnableFactory = runnableFactory;
    }

    public CacheRequest cacheInspectPageRequest(UUID uuid, String html) {
        return new CacheInspectPageRequest(
                uuid, html,
                config.get(), processing.get(),
                serverInfo.get(),
                htmlExport.get(), jsonExport.get()
        );
    }

    @Deprecated
    public CacheRequest cacheInspectPluginsTabRequest(UUID uuid, String nav, String html) {
        return new CacheInspectPluginsTabRequest(uuid, nav, html);
    }

    public GenerateRequest generateInspectPageRequest(UUID uuid) {
        return new GenerateInspectPageRequest(uuid, this, responseFactory.get(), pageFactory.get(), infoSystem.get());
    }

    @Deprecated
    public GenerateInspectPluginsTabRequest generateInspectPluginsTabRequest(UUID uuid) {
        return new GenerateInspectPluginsTabRequest(uuid, infoSystem.get(), this, pageFactory.get());
    }

    public SaveDBSettingsRequest saveDBSettingsRequest() {
        return new SaveDBSettingsRequest(plugin.get(), config.get(), serverInfo.get(), logger.get(), runnableFactory.get());
    }

    public SetupRequest sendDBSettingsRequest(String addressOfThisServer) {
        return new SendDBSettingsRequest(addressOfThisServer, serverInfo.get(), this, connectionSystem.get());
    }

    public CheckConnectionRequest checkConnectionRequest(String webAddress) {
        return new CheckConnectionRequest(webAddress, serverInfo.get(), connectionSystem.get());
    }

    @Singleton
    public static class Handlers {

        private final InfoRequestFactory factory;

        @Inject
        public Handlers(InfoRequestFactory factory) {
            this.factory = factory;
        }

        CacheRequest cacheInspectPageRequest() {
            return new CacheInspectPageRequest(
                    factory.config.get(),
                    factory.processing.get(),
                    factory.serverInfo.get(),
                    factory.htmlExport.get(),
                    factory.jsonExport.get()
            );
        }

        CacheRequest cacheInspectPluginsTabRequest() {
            return new CacheInspectPluginsTabRequest();
        }

        CheckConnectionRequest checkConnectionRequest() {
            return new CheckConnectionRequest(factory.serverInfo.get(), factory.connectionSystem.get());
        }

        GenerateRequest generateInspectPageRequest() {
            return new GenerateInspectPageRequest(
                    factory,
                    factory.responseFactory.get(),
                    factory.pageFactory.get(),
                    factory.infoSystem.get()
            );
        }

        GenerateRequest generateInspectPluginsTabRequest() {
            return new GenerateInspectPluginsTabRequest(
                    factory.infoSystem.get(),
                    factory,
                    factory.pageFactory.get()
            );
        }

        SetupRequest saveDBSettingsRequest() {
            return new SaveDBSettingsRequest(
                    factory.plugin.get(),
                    factory.config.get(),
                    factory.serverInfo.get(),
                    factory.logger.get(),
                    factory.runnableFactory.get()
            );
        }

        SetupRequest sendDBSettingsRequest() {
            return new SendDBSettingsRequest(
                    factory.serverInfo.get(),
                    factory,
                    factory.connectionSystem.get()
            );
        }
    }
}