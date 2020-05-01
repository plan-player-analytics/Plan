package com.djrapitops.plan.delivery.webserver.resolver.auth;

import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.auth.ActiveCookieStore;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.exceptions.WebUserAuthException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class LogoutResolver implements Resolver {

    @Inject
    public LogoutResolver() {}

    @Override
    public boolean canAccess(Request request) {
        return true;
    }

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
                .setHeader("Set-Cookie", "auth=" + cookie + "; Max-Age=1")
                .redirectTo("/login")
                .build();
    }
}
