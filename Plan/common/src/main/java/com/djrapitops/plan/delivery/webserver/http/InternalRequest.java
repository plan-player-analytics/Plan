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

import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.auth.AuthenticationExtractor;
import com.djrapitops.plan.delivery.webserver.auth.Cookie;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.utilities.dev.Untrusted;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a HTTP request.
 *
 * @see com.djrapitops.plan.delivery.web.resolver.request.Request for API based request, as this interface is for internal use.
 */
public interface InternalRequest {

    long getTimestamp();

    @Untrusted
    default String getAccessAddress(WebserverConfiguration webserverConfiguration) {
        AccessAddressPolicy accessAddressPolicy = webserverConfiguration.getAccessAddressPolicy();
        if (accessAddressPolicy == AccessAddressPolicy.X_FORWARDED_FOR_HEADER) {
            @Untrusted String fromHeader = getAccessAddressFromHeader();
            if (fromHeader == null) {
                // TODO disabled temporarily
//                webserverConfiguration.getWebserverLogMessages().warnAboutXForwardedForSecurityIssue();
                return getAccessAddressFromSocketIp();
            } else {
                return fromHeader;
            }
        }
        return getAccessAddressFromSocketIp();
    }

    Request toRequest(@Untrusted(reason = "from header sometimes") String accessAddress);

    Map<String, String> getRequestHeaders();

    List<Cookie> getCookies();

    String getMethod();

    String getAccessAddressFromSocketIp();

    String getAccessAddressFromHeader();

    String getRequestedURIString();

    default WebUser getWebUser(WebserverConfiguration webserverConfiguration, AuthenticationExtractor authenticationExtractor, @Untrusted String accessAddress) {
        return getAuthentication(webserverConfiguration, authenticationExtractor)
                .map(Authentication::getUser) // Can throw WebUserAuthException
                .map(User::toWebUser)
                .orElse(null);
    }

    default Optional<Authentication> getAuthentication(WebserverConfiguration webserverConfiguration, AuthenticationExtractor authenticationExtractor) {
        if (webserverConfiguration.isAuthenticationDisabled()) {
            return Optional.empty();
        }
        return authenticationExtractor.extractAuthentication(this);
    }

    String getRequestedPath();
}
