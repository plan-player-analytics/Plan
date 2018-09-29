package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.system.file.PlanFiles;

import java.io.IOException;

/**
 * @author Rsl1122
 * @since 4.0.0
 */
public class CSSResponse extends FileResponse {

    public CSSResponse(String fileName, PlanFiles files) throws IOException {
        super(format(fileName), files);
        super.setType(ResponseType.CSS);
        setContent(getContent());
    }
}
