package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class RedirectResponse extends Response {

    public RedirectResponse(String direct) {
        super.setHeader("HTTP/1.1 302 Found");
        super.setContent(direct);
    }

    @Override
    public void send(HttpExchange exchange, Locale locale, Theme theme) throws IOException {
        responseHeaders.set("Location", getContent());
        super.send(exchange, locale, theme);
    }
}
