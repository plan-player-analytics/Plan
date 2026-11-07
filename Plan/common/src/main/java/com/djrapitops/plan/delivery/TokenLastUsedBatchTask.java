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
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.sql.tables.webuser.ApiTokenTable;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Batches API token last-used timestamp updates and flushes them to the database periodically.
 * <p>
 * Instead of executing a synchronous DB update on every authenticated request,
 * this task collects token timestamps in a concurrent map and flushes them once per minute,
 * keeping only the latest timestamp per token.
 *
 * @author AuroraLS3
 */
@Singleton
public class TokenLastUsedBatchTask extends TaskSystem.Task {

    private final DBSystem dbSystem;
    private final AtomicLong lastRun = new AtomicLong(0L);

    private final Map<String, Long> tokenLastUsed = new ConcurrentHashMap<>();

    @Inject
    public TokenLastUsedBatchTask(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    /**
     * Record that a token was used at the given timestamp.
     *
     * @param token     the API token
     * @param timestamp the time the token was used
     */
    public void recordTokenUsed(String token, long timestamp) {
        synchronized (lastRun) {
            // Don't hold entries in memory if task has crashed
            if (System.currentTimeMillis() - lastRun.get() < TimeUnit.SECONDS.toMillis(15)) {
                tokenLastUsed.merge(token, timestamp, Math::max);
            } else {
                tokenLastUsed.clear();
            }
        }
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        runnableFactory.create(this)
                .runTaskTimerAsynchronously(60, 60, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        lastRun.set(System.currentTimeMillis());
        Map<String, Long> entries = tokenLastUsed;
        if (!entries.isEmpty()) {
            for (Map.Entry<String, Long> entry : entries.entrySet()) {
                dbSystem.getDatabase().executeInTransaction(
                        ApiTokenTable.UPDATE_LAST_USED_STATEMENT,
                        entry.getValue(),
                        entry.getKey()
                );
            }
        }
        tokenLastUsed.clear();
    }
}
