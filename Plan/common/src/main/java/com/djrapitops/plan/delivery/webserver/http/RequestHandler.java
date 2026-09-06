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
import com.djrapitops.plan.delivery.webserver.RateLimitGuard;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.ResponseResolver;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.apache.commons.lang3.Strings;
import org.eclipse.jetty.http.HttpHeader;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class RequestHandler {

    private final WebserverConfiguration webserverConfiguration;
    private final ResponseFactory responseFactory;
    private final ResponseResolver responseResolver;

    private final PassBruteForceGuard bruteForceGuard;
    private final RateLimitGuard rateLimitGuard;
    private final AccessLogger accessLogger;

    @Inject
    public RequestHandler(WebserverConfiguration webserverConfiguration, ResponseFactory responseFactory, ResponseResolver responseResolver, AccessLogger accessLogger) {
        this.webserverConfiguration = webserverConfiguration;
        this.responseFactory = responseFactory;
        this.responseResolver = responseResolver;
        this.accessLogger = accessLogger;

        bruteForceGuard = new PassBruteForceGuard();
        rateLimitGuard = new RateLimitGuard();
    }

    public CompletableFuture<Response> getResponse(InternalRequest internalRequest) {
        @Untrusted(reason = "from header") String accessAddress = internalRequest.getAccessAddress(webserverConfiguration);
        @Untrusted String requestedPath = internalRequest.getRequestedPathAndQuery();

        boolean blocked = false;
        CompletableFuture<Response> response;
        @Untrusted Request request = null;
        if (bruteForceGuard.shouldPreventRequest(accessAddress)) {
            response = CompletableFuture.completedFuture(responseFactory.failedLoginAttempts403());
            blocked = true;
        } else if (rateLimitGuard.shouldPreventRequest(requestedPath, accessAddress)) {
            response = CompletableFuture.completedFuture(responseFactory.failedRateLimit403());
            blocked = true;
        } else if (!webserverConfiguration.getAllowedIpList().isAllowed(accessAddress)) {
            webserverConfiguration.getWebserverLogMessages()
                    .warnAboutWhitelistBlock(accessAddress, internalRequest.getRequestedURIString());
            response = CompletableFuture.completedFuture(responseFactory.ipWhitelist403(accessAddress));
        } else {
            request = internalRequest.toRequest(accessAddress);
            response = attemptToResolve(request, accessAddress)
                    .exceptionallyCompose(throwable -> {
                        Throwable cause = throwable;
                        while (cause instanceof java.util.concurrent.CompletionException || cause instanceof java.util.concurrent.ExecutionException) {
                            if (cause.getCause() != null) {
                                cause = cause.getCause();
                            } else {
                                break;
                            }
                        }
                        if (cause instanceof WebUserAuthException thrownByAuthentication) {
                            return CompletableFuture.completedFuture(
                                    processFailedAuthentication(internalRequest, accessAddress, thrownByAuthentication));
                        }
                        return CompletableFuture.failedFuture(throwable);
                    });
        }

        response.whenComplete((r, e) -> {
            if (r != null) {
                Map<String, String> headers = r.getHeaders();
                headers.putIfAbsent("Access-Control-Allow-Origin", webserverConfiguration.getAllowedCorsOrigin());
                headers.putIfAbsent("Access-Control-Allow-Methods", "GET, OPTIONS");
                headers.putIfAbsent("Access-Control-Allow-Credentials", "true");
                headers.putIfAbsent("X-Robots-Tag", "noindex, nofollow");
            }
        });

        if (!blocked) {
            final Request doneRequest = request;
            response.whenComplete((r, e) -> accessLogger.log(internalRequest, doneRequest, r));
        }

        return response;
    }

    private CompletableFuture<Response> attemptToResolve(@Untrusted Request request, @Untrusted String accessAddress) {
        CompletableFuture<Response> response;
        try {
            response = protocolUpgradeResponse(request)
                    .orElseGet(() -> responseResolver.getResponse(request));
        } catch (Throwable t) {
            response = responseResolver.handleException(request, t);
        }
        response.whenComplete((r, e) -> {
            if (r != null) {
                request.getUser().ifPresent(user -> processSuccessfulLogin(r.getCode(), accessAddress));
            }
        });
        return response;
    }

    private Optional<CompletableFuture<Response>> protocolUpgradeResponse(@Untrusted Request request) {
        @Untrusted Optional<String> upgrade = request.getHeader(HttpHeader.UPGRADE.asString());
        if (upgrade.isPresent()) {
            @Untrusted String value = upgrade.get();
            if ("h2c".equals(value) || "h2".equals(value)) {
                return Optional.of(CompletableFuture.completedFuture(Response.builder()
                        .setStatus(101)
                        .setHeader("Connection", HttpHeader.UPGRADE.asString())
                        .setHeader(HttpHeader.UPGRADE.asString(), value)
                        .build()));
            }
        }
        return Optional.empty();
    }

    private Response processFailedAuthentication(InternalRequest internalRequest, @Untrusted String accessAddress, WebUserAuthException thrownByAuthentication) {
        FailReason failReason = thrownByAuthentication.getFailReason();
        if (failReason == FailReason.USER_PASS_MISMATCH) {
            return processWrongPassword(accessAddress, failReason);
        } else {
            @Untrusted String from = internalRequest.getRequestedURIString();
            String directTo = Strings.CI.startsWithAny(from, "/auth/", "/login") ? "/login" : "/login?from=." + from;
            return Response.builder()
                    .redirectTo(directTo)
                    .setHeader("Set-Cookie", "auth=expired; Path=/; Max-Age=0; SameSite=Lax; Secure; HTTPOnly;")
                    .build();
        }
    }

    private Response processWrongPassword(@Untrusted String accessAddress, FailReason failReason) {
        bruteForceGuard.increaseAttemptCountOnFailedLogin(accessAddress);
        if (bruteForceGuard.shouldPreventRequest(accessAddress)) {
            return responseFactory.failedLoginAttempts403();
        } else {
            return responseFactory.badRequest(failReason.getReason(), "/auth/login");
        }
    }

    private void processSuccessfulLogin(int responseCode, @Untrusted String accessAddress) {
        boolean successfulLogin = responseCode != 401;
        boolean notForbidden = responseCode != 403;
        if (successfulLogin && notForbidden) {
            bruteForceGuard.resetAttemptCount(accessAddress);
        }
    }

}
