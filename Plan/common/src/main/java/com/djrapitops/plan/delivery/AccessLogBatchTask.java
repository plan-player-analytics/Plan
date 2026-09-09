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
package com.djrapitops.plan.delivery;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.webserver.http.AccessLogger;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.StoreRequestTransaction;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author AuroraLS3
 */
@Singleton
public class AccessLogBatchTask extends TaskSystem.Task {

    private final DBSystem dbSystem;
    private final AtomicLong lastRun = new AtomicLong(0L);

    private Set<AccessLogger.LoggedRequest> loggedRequests = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Inject
    public AccessLogBatchTask(DBSystem dbSystem) {this.dbSystem = dbSystem;}

    public void addLoggedRequest(AccessLogger.LoggedRequest request) {
        synchronized (lastRun) {
            // Don't hold requests in memory if task has crashed
            if (System.currentTimeMillis() - lastRun.get() < TimeUnit.SECONDS.toMillis(15)) {
                loggedRequests.add(request);
            } else {
                loggedRequests.clear();
            }
        }
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        runnableFactory.create(this)
                .runTaskTimerAsynchronously(5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        synchronized (lastRun) {
            lastRun.set(System.currentTimeMillis());
            Set<AccessLogger.LoggedRequest> requests = loggedRequests;
            loggedRequests = Collections.newSetFromMap(new ConcurrentHashMap<>());
            dbSystem.getDatabase().executeTransaction(new StoreRequestTransaction(requests));
        }
    }
}
