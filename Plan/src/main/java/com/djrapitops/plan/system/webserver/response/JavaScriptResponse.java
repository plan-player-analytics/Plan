package com.djrapitops.plan.system.webserver.response;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class JavaScriptResponse extends FileResponse {

    JavaScriptResponse(String fileName) {
        super(format(fileName));
        super.setType(ResponseType.JAVASCRIPT);
    }
}
