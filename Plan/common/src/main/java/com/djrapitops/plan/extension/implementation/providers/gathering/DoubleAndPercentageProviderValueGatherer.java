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
import com.djrapitops.plan.extension.implementation.providers.*;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerDoubleResultTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerPercentageResultTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StoreServerDoubleResultTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StoreServerPercentageResultTransaction;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
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

    DoubleAndPercentageProviderValueGatherer(
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

    void gatherDoubleDataOfPlayer(UUID playerUUID, String playerName, Conditions conditions) {
        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<Double>, Callable<Double>> methodCaller = method -> () -> method.callMethod(extension, Parameters.player(playerUUID, playerName));
        BiFunction<MethodWrapper<Double>, Double, Transaction> percStoreTransactionCreator = (method, result) -> new StorePlayerPercentageResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result);
        BiFunction<MethodWrapper<Double>, Double, Transaction> doubleStoreTransactionCreator = (method, result) -> new StorePlayerDoubleResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result);

        for (DataProvider<Double> doubleProvider : dataProviders.getPlayerMethodsByType(Double.class)) {
            gatherDoubleDataOfProvider(methodCaller, percStoreTransactionCreator, doubleStoreTransactionCreator, conditions, doubleProvider);
        }
    }

    void gatherDoubleDataOfServer(Conditions conditions) {
        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<Double>, Callable<Double>> methodCaller = method -> () -> method.callMethod(extension, Parameters.server());
        BiFunction<MethodWrapper<Double>, Double, Transaction> percStoreTransactionCreator = (method, result) -> new StoreServerPercentageResultTransaction(pluginName, serverUUID, method.getMethodName(), result);
        BiFunction<MethodWrapper<Double>, Double, Transaction> doubleStoreTransactionCreator = (method, result) -> new StoreServerDoubleResultTransaction(pluginName, serverUUID, method.getMethodName(), result);

        for (DataProvider<Double> doubleProvider : dataProviders.getServerMethodsByType(Double.class)) {
            gatherDoubleDataOfProvider(methodCaller, percStoreTransactionCreator, doubleStoreTransactionCreator, conditions, doubleProvider);
        }
    }

    private void gatherDoubleDataOfProvider(
            Function<MethodWrapper<Double>, Callable<Double>> methodCaller,
            BiFunction<MethodWrapper<Double>, Double, Transaction> percStoreTransactionCreator,
            BiFunction<MethodWrapper<Double>, Double, Transaction> doubleStoreTransactionCreator,
            Conditions conditions, DataProvider<Double> doubleProvider

    ) {
        ProviderInformation providerInformation = doubleProvider.getProviderInformation();
        Optional<String> condition = providerInformation.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        MethodWrapper<Double> method = doubleProvider.getMethod();
        Double result = getMethodResult(methodCaller.apply(method), method);
        if (result == null) {
            return; // Error during call
        }

        database.executeTransaction(new StoreIconTransaction(providerInformation.getIcon()));
        database.executeTransaction(new StoreProviderTransaction(doubleProvider, serverUUID));

        if (doubleProvider instanceof PercentageDataProvider) {
            database.executeTransaction(percStoreTransactionCreator.apply(method, result));
        } else {
            database.executeTransaction(doubleStoreTransactionCreator.apply(method, result));
        }
    }

    private <T> T getMethodResult(Callable<T> callable, MethodWrapper<T> method) {
        try {
            return callable.call();
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
            throw new DataExtensionMethodCallException(e, pluginName, method);
        }
    }
}