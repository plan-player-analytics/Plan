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
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.gathering.domain.MinecraftStatistics;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.StatisticsQueries;
import com.djrapitops.plan.storage.database.transactions.events.StoreMinecraftStatisticsTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreMinecraftStatisticsValuesTransaction;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import org.jspecify.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cache statistic ids for insert/update.
 *
 * @author AuroraLS3
 */
@Singleton
public class StatisticsIdCache implements SubSystem {

    private final PlanFiles files;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;

    private Map<String, Integer> idByStatisticName = new HashMap<>();

    @Inject
    public StatisticsIdCache(PlanFiles files, DBSystem dbSystem, ErrorLogger errorLogger) {
        this.files = files;
        this.dbSystem = dbSystem;
        this.errorLogger = errorLogger;
    }

    @Override
    public void enable() {
        updateCache();
    }

    public @Nullable Integer getStatisticId(String statisticName) {
        Integer found = idByStatisticName.get(statisticName);
        if (found == null) {
            updateCache();
            found = idByStatisticName.get(statisticName);
        }
        return found;
    }

    public Map<Integer, Integer> getStatisticsById(Map<String, Integer> statisticsByName) {
        return statisticsByName.entrySet().stream()
                .collect(Collectors.toMap(entry -> getStatisticId(entry.getKey()), Map.Entry::getValue));
    }

    public Set<String> missingKeys(Collection<String> keys) {
        Set<String> result = new HashSet<>(keys);
        result.removeAll(idByStatisticName.keySet());
        return result;
    }

    public void storePlayerStatistics(UUID playerUUID, ServerUUID serverUUID) {
        files.getPlayerStatisticsFile(playerUUID)
                .ifPresent(statistics -> {
                    try {
                        Map<String, Integer> parsed = MinecraftStatistics.parse(statistics);
                        Set<String> missing = missingKeys(parsed.keySet());
                        if (!missing.isEmpty()) {
                            dbSystem.getDatabase().executeTransaction(new StoreMinecraftStatisticsTransaction(missing)).join();
                        }
                        Map<Integer, Integer> statisticsById = getStatisticsById(parsed);
                        dbSystem.getDatabase().executeTransaction(new StoreMinecraftStatisticsValuesTransaction(
                                new MinecraftStatistics(playerUUID, serverUUID, statisticsById)
                        ));
                    } catch (DBOpException | IOException e) {
                        errorLogger.error(e, ErrorContext.builder()
                                .related(statistics.getResourceName())
                                .build());
                    }
                });
    }

    private synchronized void updateCache() {
        idByStatisticName = dbSystem.getDatabase().query(StatisticsQueries.fetchStatisticNameToId());
    }

    @Override
    public void disable() {
        idByStatisticName.clear();
    }
}
