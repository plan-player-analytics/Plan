package main.java.com.djrapitops.plan.systems.webserver.response;

import main.java.com.djrapitops.plan.systems.webserver.theme.Theme;

/**
 * @author Rsl1122
 * @since 4.0.0
 */
public class CSSResponse extends FileResponse {

    public CSSResponse(String fileName) {
        super(fileName);
        setContent(Theme.replaceColors(getContent()));
    }
}
