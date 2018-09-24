package com.djrapitops.plan.system.webserver.response;

import java.io.IOException;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class JavaScriptResponse extends FileResponse {

    JavaScriptResponse(String fileName) throws IOException {
        super(format(fileName));
        super.setType(ResponseType.JAVASCRIPT);
    }
}
