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

import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.utilities.dev.Untrusted;

import java.util.Objects;

public class CookieAuthentication implements Authentication {

    private final ActiveCookieStore activeCookieStore;
    @Untrusted
    private final String cookie;
    private final String accessAddress;

    public CookieAuthentication(ActiveCookieStore activeCookieStore, @Untrusted String cookie, String accessAddress) {
        this.activeCookieStore = activeCookieStore;
        this.cookie = cookie;
        this.accessAddress = accessAddress;
    }

    @Override
    public User getUser() {
        return activeCookieStore.findCookie(cookie)
                // Prevents another IP from using a cookie granted to one IP
                .filter(cookieMetadata -> Objects.equals(cookieMetadata.getIpAddress(), accessAddress))
                .map(CookieMetadata::getUser).orElse(null);
    }
}
