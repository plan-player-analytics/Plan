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

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class AllowedIpList {

    private final PlanConfig config;
    private final AtomicReference<List<String>> allowList = new AtomicReference<>(null);

    @Inject
    public AllowedIpList(PlanConfig config) {
        this.config = config;
    }

    private synchronized void prepare() {
        if (allowList.get() == null) {
            allowList.set(config.isTrue(WebserverSettings.IP_WHITELIST)
                    ? config.get(WebserverSettings.WHITELIST)
                    : Collections.emptyList());
        }
    }

    public boolean isAllowed(@Untrusted String accessAddress) {
        prepare();

        List<String> ips = allowList.get();

        return ips.isEmpty() || ips.contains(accessAddress);
    }
}
