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

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import dagger.Lazy;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class ActiveCookieExpiryCleanupTask extends TaskSystem.Task {

    private final PlanConfig config;
    private final Lazy<ActiveCookieStore> activeCookieStore;
    private final PluginLogger logger;

    private final Map<String, Long> expiryDates;
    private final Formatter<Long> dateFormatter;

    @Inject
    public ActiveCookieExpiryCleanupTask(
            PlanConfig config,
            Lazy<ActiveCookieStore> activeCookieStore,
            Formatters formatters,
            PluginLogger logger
    ) {
        this.config = config;
        this.activeCookieStore = activeCookieStore;
        this.logger = logger;
        this.expiryDates = new ConcurrentHashMap<>();
        dateFormatter = formatters.secondLong();
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        runnableFactory.create(this)
                .runTaskTimerAsynchronously(
                        TimeAmount.toTicks(5, TimeUnit.SECONDS),
                        TimeAmount.toTicks(1, TimeUnit.SECONDS));
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();

        Set<String> cookiesToRemove = new HashSet<>();
        for (Map.Entry<String, Long> entry : expiryDates.entrySet()) {
            Long expiryTime = entry.getValue();
            if (expiryTime <= time) {
                String cookie = entry.getKey();
                cookiesToRemove.add(cookie);
            }
        }

        for (String cookie : cookiesToRemove) {
            activeCookieStore.get().removeCookie(cookie);
            expiryDates.remove(cookie);
            if (config.isTrue(PluginSettings.DEV_MODE)) {
                logger.info("Cookie " + cookie + " has expired: " + dateFormatter.apply(time));
            }
        }
    }

    public void addExpiry(String cookie, Long time) {
        expiryDates.put(cookie, time);
        if (config.isTrue(PluginSettings.DEV_MODE)) {
            logger.info("Cookie " + cookie + " will expire " + dateFormatter.apply(time));
        }
    }
}
