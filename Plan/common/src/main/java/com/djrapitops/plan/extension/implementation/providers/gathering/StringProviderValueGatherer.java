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
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.extension.implementation.providers.DataProviders;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.providers.StringDataProvider;
import com.djrapitops.plan.extension.implementation.results.player.Conditions;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreStringProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerStringResultTransaction;
import com.djrapitops.plugin.logging.console.PluginLogger;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Gathers StringProvider method data.
 *
 * @author Rsl1122
 */
class StringProviderValueGatherer {

    private final String pluginName;
    private final DataExtension extension;
    private final UUID serverUUID;

    private final Database database;
    private final DataProviders dataProviders;
    private final PluginLogger logger;

    StringProviderValueGatherer(
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

    void gatherStringData(UUID playerUUID, String playerName, Conditions conditions) {
        for (DataProvider<String> stringProvider : dataProviders.getPlayerMethodsByType(String.class)) {
            ProviderInformation providerInformation = stringProvider.getProviderInformation();
            Optional<String> condition = providerInformation.getCondition();
            if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
                continue; // Condition not met
            }

            MethodWrapper<String> method = stringProvider.getMethod();
            String result = getMethodResult(
                    () -> method.callMethod(extension, playerUUID, playerName),
                    throwable -> pluginName + " has invalid implementation, method " + method.getMethodName() + " threw exception: " + throwable.toString()
            );
            if (result == null) {
                continue;
            }

            result = StringUtils.truncate(result, 50);

            database.executeTransaction(new StoreIconTransaction(providerInformation.getIcon()));
            database.executeTransaction(new StoreStringProviderTransaction(stringProvider, StringDataProvider.isPlayerName(stringProvider), serverUUID));
            database.executeTransaction(new StorePlayerStringResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result));
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