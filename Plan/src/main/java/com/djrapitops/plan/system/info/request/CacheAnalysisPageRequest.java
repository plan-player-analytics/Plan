/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.Processors;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.pages.AnalysisPageResponse;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used to place HTML of a server to ResponseCache.
 *
 * @author Rsl1122
 */
public class CacheAnalysisPageRequest extends InfoRequestWithVariables implements CacheRequest {

    private final PlanConfig config;
    private final Processing processing;
    private final Processors processors;
    private final HtmlExport htmlExport;

    private UUID serverUUID;
    private String html;

    CacheAnalysisPageRequest(
            PlanConfig config,
            Processing processing,
            Processors processors,
            HtmlExport htmlExport
    ) {
        this.config = config;
        this.processing = processing;
        this.processors = processors;
        this.htmlExport = htmlExport;
    }

    CacheAnalysisPageRequest(
            UUID serverUUID, String html,
            PlanConfig config,
            Processing processing,
            Processors processors,
            HtmlExport htmlExport
    ) {
        this.config = config;
        this.processing = processing;
        this.processors = processors;
        this.htmlExport = htmlExport;

        Verify.nullCheck(serverUUID, html);
        this.serverUUID = serverUUID;
        variables.put("html", Base64Util.encode(html));
        this.html = html;
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, html (Base64)

        UUID sender = UUID.fromString(variables.get("sender"));

        String sentHtml = variables.get("html");
        Verify.nullCheck(sentHtml, () -> new BadRequestException("HTML 'html' variable not supplied in the request"));

        cache(sender, Base64Util.decode(sentHtml));
        return DefaultResponses.SUCCESS.get();
    }

    private void cache(UUID serverUUID, String html) {
        ResponseCache.cacheResponse(PageId.SERVER.of(serverUUID), () -> new AnalysisPageResponse(html));
        processing.submitNonCritical(processors.info().networkPageUpdateProcessor());

        if (config.isTrue(Settings.ANALYSIS_EXPORT)) {
            processing.submitNonCritical(() -> htmlExport.exportServer(serverUUID));
        }
    }

    @Override
    public void runLocally() {
        cache(serverUUID, html);
    }
}
