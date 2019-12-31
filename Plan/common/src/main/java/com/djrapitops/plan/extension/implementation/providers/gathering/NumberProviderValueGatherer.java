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

import com.djrapitops.plan.exceptions.DataExtensionMethodCallException;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.extension.implementation.providers.DataProviders;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerNumberResultTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StoreServerNumberResultTransaction;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Gathers NumberProvider method data.
 *
 * @author Rsl1122
 */
class NumberProviderValueGatherer {

    private final String pluginName;
    private final DataExtension extension;
    private final UUID serverUUID;

    private final Database database;
    private final DataProviders dataProviders;

    NumberProviderValueGatherer(
            String pluginName, DataExtension extension,
            UUID serverUUID, Database database,
            DataProviders dataProviders
    ) {
        this.pluginName = pluginName;
        this.extension = extension;
        this.serverUUID = serverUUID;
        this.database = database;
        this.dataProviders = dataProviders;
    }

    void gatherNumberDataOfPlayer(UUID playerUUID, String playerName, Conditions conditions) {
        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<Long>, Callable<Long>> methodCaller = method -> () -> method.callMethod(extension, Parameters.player(playerUUID, playerName));
        BiFunction<MethodWrapper<Long>, Long, Transaction> storeTransactionCreator = (method, result) -> new StorePlayerNumberResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result);

        for (DataProvider<Long> numberProvider : dataProviders.getPlayerMethodsByType(Long.class)) {
            gatherNumberDataOfProvider(methodCaller, storeTransactionCreator, conditions, numberProvider);
        }
    }

    void gatherNumberDataOfServer(Conditions conditions) {
        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<Long>, Callable<Long>> methodCaller = method -> () -> method.callMethod(extension, Parameters.server());
        BiFunction<MethodWrapper<Long>, Long, Transaction> storeTransactionCreator = (method, result) -> new StoreServerNumberResultTransaction(pluginName, serverUUID, method.getMethodName(), result);

        for (DataProvider<Long> numberProvider : dataProviders.getServerMethodsByType(Long.class)) {
            gatherNumberDataOfProvider(methodCaller, storeTransactionCreator, conditions, numberProvider);
        }
    }

    private void gatherNumberDataOfProvider(
            Function<MethodWrapper<Long>, Callable<Long>> methodCaller,
            BiFunction<MethodWrapper<Long>, Long, Transaction> storeTransactionCreator,
            Conditions conditions, DataProvider<Long> numberProvider
    ) {
        ProviderInformation providerInformation = numberProvider.getProviderInformation();
        Optional<String> condition = providerInformation.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        MethodWrapper<Long> method = numberProvider.getMethod();
        Long result = getMethodResult(methodCaller.apply(method), method);
        if (result == null) {
            return; // Error during call
        }

        database.executeTransaction(new StoreIconTransaction(providerInformation.getIcon()));
        database.executeTransaction(new StoreProviderTransaction(numberProvider, serverUUID));
        database.executeTransaction(storeTransactionCreator.apply(method, result));
    }

    private <T> T getMethodResult(Callable<T> callable, MethodWrapper<T> method) {
        try {
            return callable.call();
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
            throw new DataExtensionMethodCallException(e, pluginName, method);
        }
    }
}