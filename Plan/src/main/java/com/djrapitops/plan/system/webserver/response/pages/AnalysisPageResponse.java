package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.system.webserver.response.Response;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisPageResponse extends Response {

    public AnalysisPageResponse(String html) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(html);
    }
}
