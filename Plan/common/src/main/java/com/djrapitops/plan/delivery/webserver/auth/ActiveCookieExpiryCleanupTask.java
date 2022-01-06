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
import dagger.Lazy;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class ActiveCookieExpiryCleanupTask extends TaskSystem.Task {

    private final Lazy<ActiveCookieStore> activeCookieStore;

    private final Map<String, Long> expiryDates;

    @Inject
    public ActiveCookieExpiryCleanupTask(Lazy<ActiveCookieStore> activeCookieStore) {
        this.activeCookieStore = activeCookieStore;
        this.expiryDates = new ConcurrentHashMap<>();
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
        Set<String> removed = new HashSet<>();
        for (Map.Entry<String, Long> entry : expiryDates.entrySet()) {
            Long expiryTime = entry.getValue();
            if (expiryTime >= time) {
                String cookie = entry.getKey();
                activeCookieStore.get().removeCookie(cookie);
            }
        }

        for (String removedCookie : removed) {
            expiryDates.remove(removedCookie);
        }
    }

    public void addExpiry(String cookie, Long time) {
        expiryDates.put(cookie, time);
    }
}
