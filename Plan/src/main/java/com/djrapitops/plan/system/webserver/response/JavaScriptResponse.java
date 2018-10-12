package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.system.file.PlanFiles;

import java.io.IOException;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class JavaScriptResponse extends FileResponse {

    JavaScriptResponse(String fileName, PlanFiles files) throws IOException {
        super(format(fileName), files);
        super.setType(ResponseType.JAVASCRIPT);
    }
}
