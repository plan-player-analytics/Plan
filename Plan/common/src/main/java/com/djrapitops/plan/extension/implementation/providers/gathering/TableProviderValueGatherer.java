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
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.extension.implementation.providers.DataProviders;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreTableProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerTableResultTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StoreServerTableResultTransaction;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Gathers TableProvider method data.
 *
 * @author AuroraLS3
 */
class TableProviderValueGatherer {

    private final String pluginName;
    private final DataExtension extension;
    private final UUID serverUUID;

    private final Database database;
    private final DataProviders dataProviders;

    TableProviderValueGatherer(
            String pluginName,
            UUID serverUUID, Database database,
            ExtensionWrapper extensionWrapper
    ) {
        this.pluginName = pluginName;
        this.extension = extensionWrapper.getExtension();
        this.serverUUID = serverUUID;
        this.database = database;
        this.dataProviders = extensionWrapper.getProviders();
    }

    void gatherTableDataOfPlayer(UUID playerUUID, String playerName, Conditions conditions) {
        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<Table>, Callable<Table>> methodCaller = method -> () -> method.callMethod(extension, Parameters.player(serverUUID, playerUUID, playerName));
        BiFunction<MethodWrapper<Table>, Table, Transaction> storeTransactionCreator = (method, result) -> new StorePlayerTableResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result);

        for (DataProvider<Table> tableProvider : dataProviders.getPlayerMethodsByType(Table.class)) {
            gatherTableDataOfProvider(methodCaller, storeTransactionCreator, conditions, tableProvider);
        }
    }

    void gatherTableDataOfServer(Conditions conditions) {
        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<Table>, Callable<Table>> methodCaller = method -> () -> method.callMethod(extension, Parameters.server(serverUUID));
        BiFunction<MethodWrapper<Table>, Table, Transaction> storeTransactionCreator = (method, result) -> new StoreServerTableResultTransaction(pluginName, serverUUID, method.getMethodName(), result);

        for (DataProvider<Table> tableProvider : dataProviders.getServerMethodsByType(Table.class)) {
            gatherTableDataOfProvider(methodCaller, storeTransactionCreator, conditions, tableProvider);
        }
    }

    private void gatherTableDataOfProvider(
            Function<MethodWrapper<Table>, Callable<Table>> methodCaller,
            BiFunction<MethodWrapper<Table>, Table, Transaction> storeTransactionCreator,
            Conditions conditions,
            DataProvider<Table> tableProvider
    ) {
        ProviderInformation providerInformation = tableProvider.getProviderInformation();
        Optional<String> condition = providerInformation.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        MethodWrapper<Table> method = tableProvider.getMethod();
        Table result = getMethodResult(methodCaller.apply(method), method);
        if (result == null) {
            return; // Error during call
        }

        for (Icon icon : result.getIcons()) {
            if (icon != null) {
                database.executeTransaction(new StoreIconTransaction(icon));
            }
        }
        database.executeTransaction(new StoreTableProviderTransaction(serverUUID, providerInformation, result));
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