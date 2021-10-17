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
package com.djrapitops.plan.delivery.webserver.http;

import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.PassBruteForceGuard;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.ResponseResolver;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RequestHandler {

    private final WebserverConfiguration webserverConfiguration;
    private final ResponseFactory responseFactory;
    private final ResponseResolver responseResolver;

    private final PassBruteForceGuard bruteForceGuard;

    @Inject
    public RequestHandler(WebserverConfiguration webserverConfiguration, ResponseFactory responseFactory, ResponseResolver responseResolver) {
        this.webserverConfiguration = webserverConfiguration;
        this.responseFactory = responseFactory;
        this.responseResolver = responseResolver;

        bruteForceGuard = new PassBruteForceGuard();
    }

    public Response getResponse(InternalRequest internalRequest) {
        String accessAddress = internalRequest.getAccessAddress(webserverConfiguration);

        if (bruteForceGuard.shouldPreventRequest(accessAddress)) {
            return responseFactory.failedLoginAttempts403();
        }

        if (!webserverConfiguration.getAllowedIpList().isAllowed(accessAddress)) {
            webserverConfiguration.getInvalidConfigurationWarnings()
                    .warnAboutWhitelistBlock(accessAddress, internalRequest.getRequestedURIString());
            return responseFactory.ipWhitelist403(accessAddress);
        }

        Response response = attemptToResolve(internalRequest);

        response.getHeaders().putIfAbsent("Access-Control-Allow-Origin", webserverConfiguration.getAllowedCorsOrigin());
        response.getHeaders().putIfAbsent("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.getHeaders().putIfAbsent("Access-Control-Allow-Credentials", "true");
        response.getHeaders().putIfAbsent("X-Robots-Tag", "noindex, nofollow");

        return response;
    }

    private Response attemptToResolve(InternalRequest internalRequest) {
        String accessAddress = internalRequest.getAccessAddress(webserverConfiguration);
        try {
            Request request = internalRequest.toRequest();
            Response response = responseResolver.getResponse(request);
            request.getUser().ifPresent(user -> processSuccessfulLogin(response.getCode(), accessAddress));
            return response;
        } catch (WebUserAuthException thrownByAuthentication) {
            return processFailedAuthentication(internalRequest, accessAddress, thrownByAuthentication);
        }
    }

    private Response processFailedAuthentication(InternalRequest internalRequest, String accessAddress, WebUserAuthException thrownByAuthentication) {
        FailReason failReason = thrownByAuthentication.getFailReason();
        if (failReason == FailReason.USER_PASS_MISMATCH) {
            return processWrongPassword(accessAddress, failReason);
        } else {
            String from = internalRequest.getRequestedURIString();
            String directTo = StringUtils.startsWithAny(from, "/auth/", "/login") ? "/login" : "/login?from=." + from;
            return Response.builder()
                    .redirectTo(directTo)
                    .setHeader("Set-Cookie", "auth=expired; Path=/; Max-Age=1; SameSite=Lax; Secure;")
                    .build();
        }
    }

    private Response processWrongPassword(String accessAddress, FailReason failReason) {
        bruteForceGuard.increaseAttemptCountOnFailedLogin(accessAddress);
        if (bruteForceGuard.shouldPreventRequest(accessAddress)) {
            return responseFactory.failedLoginAttempts403();
        } else {
            return responseFactory.badRequest(failReason.getReason(), "/auth/login");
        }
    }

    private void processSuccessfulLogin(int responseCode, String accessAddress) {
        boolean successfulLogin = responseCode != 401;
        boolean notForbidden = responseCode != 403;
        if (successfulLogin && notForbidden) {
            bruteForceGuard.resetAttemptCount(accessAddress);
        }
    }

}
