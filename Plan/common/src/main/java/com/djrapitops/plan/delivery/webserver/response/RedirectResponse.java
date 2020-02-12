/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver.response;

import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * @author Rsl1122
 */
public class RedirectResponse extends Response_old {

    private final String direct;

    public RedirectResponse(String direct) {
        super.setHeader("HTTP/1.1 302 Found");
        this.direct = direct;
    }

    @Override
    public void send(HttpExchange exchange, Locale locale, Theme theme) throws IOException {
        responseHeaders.set("Location", direct);
        super.send(exchange, locale, theme);
    }
}
