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
package com.djrapitops.plan.delivery.webserver.resolver.auth;

import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.auth.ActiveCookieStore;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.exceptions.WebUserAuthException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class LogoutResolver implements NoAuthResolver {

    @Inject
    public LogoutResolver() {}

    @Override
    public Optional<Response> resolve(Request request) {
        String cookies = request.getHeader("Cookie").orElse("");
        String foundCookie = null;
        for (String cookie : cookies.split(";")) {
            if (cookie.isEmpty()) continue;
            String[] split = cookie.split("=");
            String name = split[0];
            String value = split[1];
            if ("auth".equals(name)) {
                foundCookie = value;
                ActiveCookieStore.removeCookie(value);
            }
        }

        if (foundCookie == null) {
            throw new WebUserAuthException(FailReason.NO_USER_PRESENT);
        }
        return Optional.of(getResponse(foundCookie));
    }

    public Response getResponse(String cookie) {
        return Response.builder()
                .setStatus(200)
                .setHeader("Set-Cookie", "auth=" + cookie + "; Max-Age=1; SameSite=Lax; Secure;")
                .setMimeType(MimeType.HTML)
                .setContent(
                        "<p>Logging out..</p><script>const urlParams = new URLSearchParams(window.location.search);" +
                                "const cause = urlParams.get('cause');" +
                                "setTimeout(() => window.location.href = cause ? '../login?cause=' + cause : '../login', 1000);" +
                                "</script>"
                )
                .build();
    }
}
