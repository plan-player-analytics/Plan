/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
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

    private final UUID serverUUID;
    private final String html;

    private CacheAnalysisPageRequest() {
        serverUUID = null;
        html = null;
    }

    public CacheAnalysisPageRequest(UUID serverUUID, String html) {
        Verify.nullCheck(serverUUID, html);
        this.serverUUID = serverUUID;
        variables.put("html", Base64Util.encode(html));
        this.html = html;
    }

    public static CacheAnalysisPageRequest createHandler() {
        return new CacheAnalysisPageRequest();
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, html (Base64)

        UUID serverUUID = UUID.fromString(variables.get("sender"));

        String html = variables.get("html");
        Verify.nullCheck(html, () -> new BadRequestException("HTML 'html' variable not supplied in the request"));

        boolean export = Settings.ANALYSIS_EXPORT.isTrue();
        cache(export, serverUUID, Base64Util.decode(html));
        return DefaultResponses.SUCCESS.get();
    }

    private void cache(boolean export, UUID serverUUID, String html) {
        ResponseCache.cacheResponse(PageId.SERVER.of(serverUUID), () -> new AnalysisPageResponse(html));
        if (export) {
            Processing.submitNonCritical(() -> HtmlExport.exportServer(serverUUID));
        }
    }

    @Override
    public void runLocally() {
        cache(Settings.ANALYSIS_EXPORT.isTrue(), serverUUID, html);
    }
}
