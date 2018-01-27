/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.TransferDatabaseException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.Processor;
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
public class CacheAnalysisPageRequest implements CacheRequest {

    private final UUID serverUUID;
    private final String html;

    private CacheAnalysisPageRequest() {
        serverUUID = null;
        html = null;
    }

    public CacheAnalysisPageRequest(UUID serverUUID, String html) {
        Verify.nullCheck(serverUUID, html);
        this.serverUUID = serverUUID;
        this.html = html;
    }

    public static CacheAnalysisPageRequest createHandler() {
        return new CacheAnalysisPageRequest();
    }

    @Override
    public void placeDataToDatabase() throws WebException {
        Verify.nullCheck(serverUUID, html);

        String encodedHtml = Base64Util.encode(html);
        try {
            Database.getActive().transfer().storeServerHtml(serverUUID, encodedHtml);
        } catch (DBException e) {
            throw new TransferDatabaseException(e);
        }
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender

        try {
            Map<UUID, String> pages = Database.getActive().transfer().getEncodedServerHtml();

            boolean export = Settings.ANALYSIS_EXPORT.isTrue();
            for (Map.Entry<UUID, String> entry : pages.entrySet()) {
                UUID serverUUID = entry.getKey();
                String html = Base64Util.decode(entry.getValue());

                cache(export, serverUUID, html);
            }
        } catch (DBException e) {
            throw new TransferDatabaseException(e);
        }
        return DefaultResponses.SUCCESS.get();
    }

    private void cache(boolean export, UUID serverUUID, String html) {
        ResponseCache.cacheResponse(PageId.SERVER.of(serverUUID), () -> new AnalysisPageResponse(html));
        if (export) {
            Processor.queue(() -> HtmlExport.exportServer(serverUUID));
        }
    }

    @Override
    public void runLocally() {
        cache(Settings.ANALYSIS_EXPORT.isTrue(), serverUUID, html);
    }
}