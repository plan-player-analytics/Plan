package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plan.utilities.html.pages.PageFactory;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link InfoRequest} objects that are used for handling received requests.
 *
 * @author Rsl1122
 */
@Singleton
public class InfoRequestHandlerFactory {

    private final Lazy<PlanPlugin> plugin;
    private final Lazy<PlanConfig> config;
    private final Lazy<Processing> processing;
    private final Lazy<InfoSystem> infoSystem;
    private final Lazy<ConnectionSystem> connectionSystem;
    private final Lazy<ServerInfo> serverInfo;
    private final Lazy<InfoRequestFactory> infoRequestFactory;
    private final Lazy<ResponseFactory> responseFactory;
    private final Lazy<PageFactory> pageFactory;
    private final Lazy<HtmlExport> htmlExport;
    private final Lazy<PluginLogger> logger;
    private final Lazy<RunnableFactory> runnableFactory;

    @Inject
    public InfoRequestHandlerFactory(
            Lazy<PlanPlugin> plugin,
            Lazy<PlanConfig> config,
            Lazy<Processing> processing,
            Lazy<InfoSystem> infoSystem,
            Lazy<ConnectionSystem> connectionSystem,
            Lazy<ServerInfo> serverInfo,
            Lazy<InfoRequestFactory> infoRequestFactory,
            Lazy<ResponseFactory> responseFactory,
            Lazy<PageFactory> pageFactory,
            Lazy<HtmlExport> htmlExport,
            Lazy<PluginLogger> logger,
            Lazy<RunnableFactory> runnableFactory
    ) {
        this.plugin = plugin;
        this.config = config;
        this.processing = processing;
        this.infoSystem = infoSystem;
        this.connectionSystem = connectionSystem;
        this.serverInfo = serverInfo;
        this.infoRequestFactory = infoRequestFactory;
        this.responseFactory = responseFactory;
        this.pageFactory = pageFactory;
        this.htmlExport = htmlExport;
        this.logger = logger;
        this.runnableFactory = runnableFactory;
    }

    CacheRequest cacheAnalysisPageRequest() {
        return new CacheAnalysisPageRequest(config.get(), processing.get(), htmlExport.get());
    }

    CacheRequest cacheInspectPageRequest() {
        return new CacheInspectPageRequest(config.get(), processing.get(), serverInfo.get(), htmlExport.get());
    }

    CacheRequest cacheInspectPluginsTabRequest() {
        return new CacheInspectPluginsTabRequest(serverInfo.get());
    }

    CacheRequest cacheNetworkPageContentRequest() {
        return new CacheNetworkPageContentRequest(serverInfo.get());
    }

    CheckConnectionRequest checkConnectionRequest() {
        return new CheckConnectionRequest(connectionSystem.get());
    }

    GenerateRequest generateAnalysisPageRequest() {
        return new GenerateAnalysisPageRequest(infoRequestFactory.get(), serverInfo.get(), infoSystem.get(), pageFactory.get());
    }

    GenerateRequest generateInspectPageRequest() {
        return new GenerateInspectPageRequest(infoRequestFactory.get(), responseFactory.get(), pageFactory.get(), infoSystem.get());
    }

    GenerateRequest generateInspectPluginsTabRequest() {
        return new GenerateInspectPluginsTabRequest(infoSystem.get(), infoRequestFactory.get(), pageFactory.get());
    }

    GenerateRequest generateNetworkPageContentRequest() {
        return new GenerateNetworkPageContentRequest(infoSystem.get());
    }

    SetupRequest saveDBSettingsRequest() {
        return new SaveDBSettingsRequest(plugin.get(), config.get(), logger.get(), runnableFactory.get());
    }

    SetupRequest sendDBSettingsRequest() {
        return new SendDBSettingsRequest(config.get(), infoRequestFactory.get(), connectionSystem.get());
    }
}