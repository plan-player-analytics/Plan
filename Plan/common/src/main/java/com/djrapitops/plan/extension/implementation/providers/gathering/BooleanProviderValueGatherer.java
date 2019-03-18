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
package com.djrapitops.plan.extension.implementation.providers.gathering;

import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.implementation.providers.BooleanDataProvider;
import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.extension.implementation.providers.DataProviders;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreBooleanProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerBooleanResultTransaction;
import com.djrapitops.plugin.logging.console.PluginLogger;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Gathers BooleanProvider method data.
 *
 * @author Rsl1122
 */
class BooleanProviderValueGatherer {

    private final String pluginName;
    private final DataExtension extension;
    private final UUID serverUUID;

    private final Database database;
    private final DataProviders dataProviders;
    private final PluginLogger logger;

    BooleanProviderValueGatherer(
            String pluginName, DataExtension extension,
            UUID serverUUID, Database database,
            DataProviders dataProviders,
            PluginLogger logger
    ) {
        this.pluginName = pluginName;
        this.extension = extension;
        this.serverUUID = serverUUID;
        this.database = database;
        this.dataProviders = dataProviders;
        this.logger = logger;
    }

    Set<String> gatherBooleanData(UUID playerUUID, String playerName) {
        Set<String> providedConditions = new HashSet<>();

        List<DataProvider<Boolean>> unsatisifiedProviders = new ArrayList<>(dataProviders.getPlayerMethodsByType(Boolean.class));
        Set<DataProvider<Boolean>> satisfied = new HashSet<>();

        do {
            // Clear conditions satisfied in previous loop
            satisfied.clear();
            // Loop through all unsatisfied providers to see if more conditions are satisfied
            for (DataProvider<Boolean> booleanProvider : unsatisifiedProviders) {
                Optional<String> condition = booleanProvider.getProviderInformation().getCondition();
                if (condition.isPresent() && !providedConditions.contains(condition.get())) {
                    continue; // Condition not met
                }

                Optional<String> providedCondition = BooleanDataProvider.getProvidedCondition(booleanProvider);

                MethodWrapper<Boolean> method = booleanProvider.getMethod();
                Boolean result = getMethodResult(
                        () -> method.callMethod(extension, playerUUID, playerName),
                        throwable -> pluginName + " has invalid implementation, method " + method.getMethodName() + " threw exception: " + throwable.toString()
                );
                if (result == null) {
                    satisfied.add(booleanProvider); // Prevents further attempts to call this provider for this player.
                    continue;
                }

                if (result && providedCondition.isPresent()) {
                    providedConditions.add(providedCondition.get()); // The condition was fulfilled for this player.
                }

                satisfied.add(booleanProvider); // Prevents further attempts to call this provider for this player.
                database.executeTransaction(new StoreIconTransaction(booleanProvider.getProviderInformation().getIcon()));
                database.executeTransaction(new StoreBooleanProviderTransaction(booleanProvider, providedCondition.orElse(null), serverUUID));
                database.executeTransaction(new StorePlayerBooleanResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result));
            }
            // Remove now satisfied Providers so that they are not called again
            unsatisifiedProviders.removeAll(satisfied);
            // If no new conditions could be satisfied, stop looping.
        } while (!satisfied.isEmpty());

        return providedConditions;
    }

    private <T> T getMethodResult(Callable<T> callable, Function<Throwable, String> errorMsg) {
        try {
            return callable.call();
        } catch (Exception | NoSuchFieldError | NoSuchMethodError e) {
            logger.warn(errorMsg.apply(e));
            return null;
        }
    }

}