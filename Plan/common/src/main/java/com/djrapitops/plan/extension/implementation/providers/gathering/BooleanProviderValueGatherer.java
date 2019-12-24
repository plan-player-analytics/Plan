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
import com.djrapitops.plan.extension.implementation.providers.BooleanDataProvider;
import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.extension.implementation.providers.DataProviders;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerBooleanResultTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StoreServerBooleanResultTransaction;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
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

    BooleanProviderValueGatherer(
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

    Conditions gatherBooleanDataOfPlayer(UUID playerUUID, String playerName) {
        Conditions conditions = new Conditions();

        List<DataProvider<Boolean>> unsatisifiedProviders = new ArrayList<>(dataProviders.getPlayerMethodsByType(Boolean.class));
        Set<DataProvider<Boolean>> satisfied;

        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<Boolean>, Callable<Boolean>> methodCaller = method -> () -> method.callMethod(extension, playerUUID, playerName);
        BiFunction<MethodWrapper<Boolean>, Boolean, Transaction> storeTrancationCreator = (method, result) -> new StorePlayerBooleanResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result);

        do {
            // Loop through all unsatisfied providers to see if more conditions are satisfied
            satisfied = attemptToSatisfyMoreConditionsAndStoreResults(methodCaller, storeTrancationCreator, conditions, unsatisifiedProviders);
            // Remove now satisfied Providers so that they are not called again
            unsatisifiedProviders.removeAll(satisfied);
            // If no new conditions could be satisfied, stop looping.
        } while (!satisfied.isEmpty());

        return conditions;
    }

    Conditions gatherBooleanDataOfServer() {
        Conditions conditions = new Conditions();

        List<DataProvider<Boolean>> unsatisifiedProviders = new ArrayList<>(dataProviders.getServerMethodsByType(Boolean.class));
        Set<DataProvider<Boolean>> satisfied;

        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<Boolean>, Callable<Boolean>> methodCaller = method -> () -> method.callMethod(extension);
        BiFunction<MethodWrapper<Boolean>, Boolean, Transaction> storeTransactionCreator = (method, result) -> new StoreServerBooleanResultTransaction(pluginName, serverUUID, method.getMethodName(), result);

        do {
            // Loop through all unsatisfied providers to see if more conditions are satisfied
            satisfied = attemptToSatisfyMoreConditionsAndStoreResults(methodCaller, storeTransactionCreator, conditions, unsatisifiedProviders);
            // Remove now satisfied Providers so that they are not called again
            unsatisifiedProviders.removeAll(satisfied);
            // If no new conditions could be satisfied, stop looping.
        } while (!satisfied.isEmpty());

        return conditions;
    }

    private Set<DataProvider<Boolean>> attemptToSatisfyMoreConditionsAndStoreResults(
            Function<MethodWrapper<Boolean>, Callable<Boolean>> methodCaller,
            BiFunction<MethodWrapper<Boolean>, Boolean, Transaction> storeTransactionCreator,
            Conditions conditions, List<DataProvider<Boolean>> unsatisifiedProviders
    ) {
        Set<DataProvider<Boolean>> satisfied = new HashSet<>();
        for (DataProvider<Boolean> booleanProvider : unsatisifiedProviders) {
            ProviderInformation providerInformation = booleanProvider.getProviderInformation();

            Optional<String> condition = providerInformation.getCondition();
            if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
                // Condition required by the BooleanProvider is not satisfied
                continue;
            }

            Optional<String> providedCondition = BooleanDataProvider.getProvidedCondition(booleanProvider);
            MethodWrapper<Boolean> method = booleanProvider.getMethod();
            Boolean result = getMethodResult(methodCaller.apply(method), method);
            if (result == null) {
                // Error during method call
                satisfied.add(booleanProvider); // Prevents further attempts to call this provider for this player.
                continue;
            }

            if (providedCondition.isPresent()) {
                if (result) {
                    // The condition was fulfilled (true) for this player.
                    conditions.conditionFulfilled(providedCondition.get());
                } else {
                    // The negated condition was fulfilled (false) for this player.
                    conditions.conditionFulfilled("not_" + providedCondition.get());
                }
            }

            satisfied.add(booleanProvider); // Prevents further attempts to call this provider for this player.
            database.executeTransaction(new StoreIconTransaction(providerInformation.getIcon()));
            database.executeTransaction(new StoreProviderTransaction(booleanProvider, serverUUID));
            database.executeTransaction(storeTransactionCreator.apply(method, result));
        }
        return satisfied;
    }

    private <T> T getMethodResult(Callable<T> callable, MethodWrapper<T> method) {
        try {
            return callable.call();
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
            throw new DataExtensionMethodCallException(e, pluginName, method);
        }
    }
}