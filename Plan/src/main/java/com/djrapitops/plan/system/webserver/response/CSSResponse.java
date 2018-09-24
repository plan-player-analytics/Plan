package com.djrapitops.plan.system.webserver.response;

import java.io.IOException;

/**
 * @author Rsl1122
 * @since 4.0.0
 */
public class CSSResponse extends FileResponse {

    public CSSResponse(String fileName) throws IOException {
        super(format(fileName));
        super.setType(ResponseType.CSS);
        setContent(getContent());
    }
}
