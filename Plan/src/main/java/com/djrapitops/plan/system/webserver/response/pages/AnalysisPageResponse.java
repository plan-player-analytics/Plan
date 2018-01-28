package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.processing.Processor;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plugin.api.utility.log.Log;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisPageResponse extends Response {

    public static AnalysisPageResponse refreshNow() {
        Processor.queue(() -> {
            try {
                InfoSystem.getInstance().generateAnalysisPageOfThisServer();
            } catch (WebException e) {
                // TODO Exception handling
                Log.toLog(AnalysisPageResponse.class, e);
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
