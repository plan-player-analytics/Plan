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

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage for authentication tokens.
 *
 * @author AuroraLS3
 */
@Singleton
public class AuthTokenStore implements SubSystem {

    private final DBSystem dbSystem;
    private final Processing processing;

    private final Map<String, List<String>> permissionsByToken;

    @Inject
    public AuthTokenStore(DBSystem dbSystem, Processing processing) {
        this.dbSystem = dbSystem;
        this.processing = processing;
        this.permissionsByToken = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() {
        refreshTokens();
    }

    public void refreshTokens() {
        processing.submitNonCritical(this::updateTokens);
    }

    private void updateTokens() {
        permissionsByToken.putAll(dbSystem.getDatabase().query(WebUserQueries.fetchTokenPermissions()));
    }

    public void addToken(String token) {
        processing.submitNonCritical(() -> {
            var permissions = dbSystem.getDatabase().query(WebUserQueries.fetchTokenPermissions(token));
            if (permissions.isEmpty()) return;
            permissionsByToken.put(token, permissions);
        });
    }

    @Override
    public void disable() {
        permissionsByToken.clear();
    }

    public List<String> getPermissions(String token) {
        return permissionsByToken.getOrDefault(token, new ArrayList<>());
    }
}
