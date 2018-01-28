package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processor;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.UUID;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisPageResponse extends Response {

    public static AnalysisPageResponse refreshNow() {
        return refreshNow(ServerInfo.getServerUUID());
    }

    public static AnalysisPageResponse refreshNow(UUID serverUUID) {
        Processor.queue(() -> {
            try {
                InfoSystem.getInstance().generateAnalysisPage(serverUUID);
            } catch (NoServersException e) {
                ResponseCache.cacheResponse(PageId.SERVER.of(serverUUID), () -> new NotFoundResponse(e.getMessage()));
            } catch (WebException e) {
                // TODO Exception handling
                Log.toLog(AnalysisPageResponse.class.getName(), e);
            }
        });
        return new AnalysisPageResponse(getRefreshingHtml());
    }

    public AnalysisPageResponse(String html) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(html);
    }

    public static String getRefreshingHtml() {
        ErrorResponse refreshPage = new ErrorResponse();
        refreshPage.setTitle("Analysis is being refreshed..");
        refreshPage.setParagraph("<meta http-equiv=\"refresh\" content=\"5\" /><i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Analysis is being run, refresh the page after a few seconds.. (F5)");
        refreshPage.replacePlaceholders();
        return refreshPage.getContent();
    }

}
