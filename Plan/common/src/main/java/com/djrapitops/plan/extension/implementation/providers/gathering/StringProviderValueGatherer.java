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
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerStringResultTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StoreServerStringResultTransaction;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
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

    StringProviderValueGatherer(
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

    void gatherStringDataOfPlayer(UUID playerUUID, String playerName, Conditions conditions) {
        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<String>, Callable<String>> methodCaller = method -> () -> method.callMethod(extension, playerUUID, playerName);
        BiFunction<MethodWrapper<String>, String, Transaction> storeTransactionCreator = (method, result) -> new StorePlayerStringResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result);

        for (DataProvider<String> stringProvider : dataProviders.getPlayerMethodsByType(String.class)) {
            gatherStringDataOfProvider(methodCaller, storeTransactionCreator, conditions, stringProvider);
        }
    }

    void gatherStringDataOfServer(Conditions conditions) {
        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<String>, Callable<String>> methodCaller = method -> () -> method.callMethod(extension);
        BiFunction<MethodWrapper<String>, String, Transaction> storeTransactionCreator = (method, result) -> new StoreServerStringResultTransaction(pluginName, serverUUID, method.getMethodName(), result);

        for (DataProvider<String> stringProvider : dataProviders.getServerMethodsByType(String.class)) {
            gatherStringDataOfProvider(methodCaller, storeTransactionCreator, conditions, stringProvider);
        }
    }

    private void gatherStringDataOfProvider(
            Function<MethodWrapper<String>, Callable<String>> methodCaller,
            BiFunction<MethodWrapper<String>, String, Transaction> storeTransactionCreator,
            Conditions conditions,
            DataProvider<String> stringProvider
    ) {
        ProviderInformation providerInformation = stringProvider.getProviderInformation();
        Optional<String> condition = providerInformation.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        MethodWrapper<String> method = stringProvider.getMethod();
        String result = getMethodResult(methodCaller.apply(method), method);
        if (result == null) {
            return; // Error during call
        }

        result = StringUtils.truncate(result, 50);

        database.executeTransaction(new StoreIconTransaction(providerInformation.getIcon()));
        database.executeTransaction(new StoreProviderTransaction(stringProvider, serverUUID));
        database.executeTransaction(storeTransactionCreator.apply(method, result));
    }

    private <T> T getMethodResult(Callable<T> callable, MethodWrapper<String> method) {
        try {
            return callable.call();
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
            throw new DataExtensionMethodCallException(e, pluginName, method);
        }
    }

}