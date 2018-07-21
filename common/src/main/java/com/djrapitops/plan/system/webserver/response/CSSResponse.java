package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.system.settings.theme.Theme;

/**
 * @author Rsl1122
 * @since 4.0.0
 */
public class CSSResponse extends FileResponse {

    public CSSResponse(String fileName) {
        super(format(fileName));
        super.setType(ResponseType.CSS);
        setContent(Theme.replaceColors(getContent()));
    }
}
