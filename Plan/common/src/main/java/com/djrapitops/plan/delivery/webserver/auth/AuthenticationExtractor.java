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
package com.djrapitops.plan.delivery.webserver.auth;

import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.delivery.webserver.http.InternalRequest;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class AuthenticationExtractor {

    private final ActiveCookieStore activeCookieStore;
    private final WebserverConfiguration webserverConfiguration;

    @Inject
    public AuthenticationExtractor(ActiveCookieStore activeCookieStore, WebserverConfiguration webserverConfiguration) {
        this.activeCookieStore = activeCookieStore;
        this.webserverConfiguration = webserverConfiguration;
    }

    public Optional<Authentication> extractAuthentication(InternalRequest internalRequest) {
        return getCookieAuthentication(internalRequest.getCookies(), internalRequest.getAccessAddress(webserverConfiguration));
    }

    private Optional<Authentication> getCookieAuthentication(@Untrusted List<Cookie> cookies, String accessAddress) {
        for (@Untrusted Cookie cookie : cookies) {
            if ("auth".equals(cookie.getName())) {
                return Optional.of(new CookieAuthentication(activeCookieStore, cookie.getValue(), accessAddress));
            }
        }
        return Optional.empty();
    }
}
