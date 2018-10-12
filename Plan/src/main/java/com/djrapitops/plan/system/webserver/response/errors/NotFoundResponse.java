package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.io.IOException;

/**
 * Generic 404 response.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class NotFoundResponse extends ErrorResponse {

    public NotFoundResponse(String msg, String version, PlanFiles files) throws IOException {
        super(version, files);
        super.setHeader("HTTP/1.1 404 Not Found");
        super.setTitle(Icon.called("map-signs") + " 404 Not Found");
        super.setParagraph(msg);
        super.replacePlaceholders();
    }

}
