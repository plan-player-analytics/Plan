package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.system.file.PlanFiles;

import java.io.IOException;

/**
 * This response is used when Analysis is being refreshed and the user needs some feedback.
 *
 * @author Rsl1122
 */
public class RefreshingAnalysisResponse extends ErrorResponse {

    public RefreshingAnalysisResponse(String version, PlanFiles files) throws IOException {
        super(version, files);

        setTitle("Analysis is being refreshed..");
        setParagraph("<meta http-equiv=\"refresh\" content=\"5\" /><i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Analysis is being run, refresh the page after a few seconds.. (F5)");
        replacePlaceholders();
    }
}