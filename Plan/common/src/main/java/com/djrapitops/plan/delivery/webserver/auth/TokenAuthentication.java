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

import com.djrapitops.plan.delivery.web.resolver.request.WebUser;

import java.util.List;

/**
 * Authentication with a token.
 *
 * @author AuroraLS3
 */
public class TokenAuthentication implements Authentication {

    private final String token;
    private final List<String> permissions;

    public TokenAuthentication(String token, List<String> permissions) {
        this.token = token;
        this.permissions = permissions;
    }

    @Override
    public WebUser getWebUser() {
        return new WebUser("console", null, token, permissions);
    }
}
