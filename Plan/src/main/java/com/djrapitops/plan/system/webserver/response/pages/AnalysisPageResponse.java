package com.djrapitops.plan.system.webserver.response.pages;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisPageResponse extends PageResponse {

    public AnalysisPageResponse(String html) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(html);
    }
}
