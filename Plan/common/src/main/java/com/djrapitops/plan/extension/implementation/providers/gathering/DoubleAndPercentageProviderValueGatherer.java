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
import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.extension.implementation.providers.DataProviders;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.providers.PercentageDataProvider;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreDoubleProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerDoubleResultTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerPercentageResultTransaction;
import com.djrapitops.plugin.logging.console.PluginLogger;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Gathers DoubleProvider and PercentageProvider method data.
 *
 * @author Rsl1122
 */
class DoubleAndPercentageProviderValueGatherer {

    private final String pluginName;
    private final DataExtension extension;
    private final UUID serverUUID;

    private final Database database;
    private final DataProviders dataProviders;
    private final PluginLogger logger;

    DoubleAndPercentageProviderValueGatherer(
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

    void gatherDoubleData(UUID playerUUID, String playerName, Set<String> providedConditions) {
        for (DataProvider<Double> doubleProvider : dataProviders.getPlayerMethodsByType(Double.class)) {
            Optional<String> condition = doubleProvider.getProviderInformation().getCondition();
            if (condition.isPresent() && !providedConditions.contains(condition.get())) {
                continue; // Condition not met
            }

            MethodWrapper<Double> method = doubleProvider.getMethod();
            Double result = getMethodResult(
                    () -> method.callMethod(extension, playerUUID, playerName),
                    throwable -> pluginName + " has invalid implementation, method " + method.getMethodName() + " threw exception: " + throwable.toString()
            );
            if (result == null) {
                continue;
            }

            database.executeTransaction(new StoreIconTransaction(doubleProvider.getProviderInformation().getIcon()));
            database.executeTransaction(new StoreDoubleProviderTransaction(doubleProvider, serverUUID));

            if (doubleProvider instanceof PercentageDataProvider) {
                database.executeTransaction(new StorePlayerPercentageResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result));
            } else {
                database.executeTransaction(new StorePlayerDoubleResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result));
            }
        }
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