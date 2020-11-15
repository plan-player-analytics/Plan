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

import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.delivery.webserver.auth.ActiveCookieStore;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.exceptions.PassEncryptException;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class LoginResolver implements NoAuthResolver {

    private final DBSystem dbSystem;

    @Inject
    public LoginResolver(
            DBSystem dbSystem
    ) {
        this.dbSystem = dbSystem;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        try {
            String cookie = ActiveCookieStore.generateNewCookie(getUser(request));
            return Optional.of(getResponse(cookie));
        } catch (DBOpException | PassEncryptException e) {
            throw new WebUserAuthException(e);
        }
    }

    public Response getResponse(String cookie) {
        return Response.builder()
                .setStatus(200)
                .setHeader("Set-Cookie", "auth=" + cookie + "; Path=/; Max-Age=" + TimeUnit.HOURS.toSeconds(2L) + "; SameSite=Lax; Secure;")
                .setJSONContent(Collections.singletonMap("success", true))
                .build();
    }

    public User getUser(Request request) {
        URIQuery query = request.getQuery();
        String username = query.get("user").orElseThrow(() -> new BadRequestException("'user' parameter not defined"));
        String password = query.get("password").orElseThrow(() -> new BadRequestException("'password' parameter not defined"));
        User user = dbSystem.getDatabase().query(WebUserQueries.fetchUser(username))
                .orElseThrow(() -> new BadRequestException(FailReason.USER_DOES_NOT_EXIST.getReason() + ": " + username));

        boolean correctPass = user.doesPasswordMatch(password);
        if (!correctPass) {
            throw new WebUserAuthException(FailReason.USER_PASS_MISMATCH);
        }
        return user;
    }
}
