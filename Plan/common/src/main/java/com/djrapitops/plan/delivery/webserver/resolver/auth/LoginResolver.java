package com.djrapitops.plan.delivery.webserver.resolver.auth;

import com.djrapitops.plan.delivery.domain.WebUser;
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
import com.djrapitops.plan.utilities.PassEncryptUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class LoginResolver implements NoAuthResolver {

    private DBSystem dbSystem;

    @Inject
    public LoginResolver(
            DBSystem dbSystem
    ) {
        this.dbSystem = dbSystem;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        try {
            String cookie = ActiveCookieStore.generateNewCookie(getWebUser(request));
            return Optional.of(getResponse(cookie));
        } catch (DBOpException | PassEncryptException e) {
            throw new WebUserAuthException(e);
        }
    }

    public Response getResponse(String cookie) {
        return Response.builder()
                .setStatus(200)
                .setHeader("Set-Cookie", "auth=" + cookie + "; Path=/; Max-Age=" + TimeUnit.HOURS.toSeconds(2L))
                .setJSONContent(Collections.singletonMap("success", true))
                .build();
    }

    public WebUser getWebUser(Request request) throws PassEncryptUtil.CannotPerformOperationException, PassEncryptUtil.InvalidHashException {
        URIQuery query = request.getQuery();
        String username = query.get("user").orElseThrow(() -> new BadRequestException("'user' parameter not defined"));
        String password = query.get("password").orElseThrow(() -> new BadRequestException("'user' parameter not defined"));
        WebUser webUser = dbSystem.getDatabase().query(WebUserQueries.fetchWebUser(username))
                .orElseThrow(() -> new BadRequestException(FailReason.USER_DOES_NOT_EXIST.getReason() + ": " + username));

        boolean correctPass = PassEncryptUtil.verifyPassword(password, webUser.getSaltedPassHash());
        if (!correctPass) {
            throw new WebUserAuthException(FailReason.USER_PASS_MISMATCH);
        }
        return webUser;
    }
}
