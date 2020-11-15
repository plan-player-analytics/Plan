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
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.extension.implementation.providers.DataProviders;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
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

    Conditions gatherBooleanDataOfPlayer(UUID playerUUID, String playerName) {
        Conditions conditions = new Conditions();

        List<DataProvider<Boolean>> unsatisfiedProviders = new ArrayList<>(dataProviders.getPlayerMethodsByType(Boolean.class));
        Set<DataProvider<Boolean>> satisfied;

        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<Boolean>, Callable<Boolean>> methodCaller = method -> () -> method.callMethod(extension, Parameters.player(serverUUID, playerUUID, playerName));
        BiFunction<MethodWrapper<Boolean>, Boolean, Transaction> storeTrancationCreator = (method, result) -> new StorePlayerBooleanResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result);

        do {
            // Loop through all unsatisfied providers to see if more conditions are satisfied
            satisfied = attemptToSatisfyMoreConditionsAndStoreResults(methodCaller, storeTrancationCreator, conditions, unsatisfiedProviders);
            // Remove now satisfied Providers so that they are not called again
            unsatisfiedProviders.removeAll(satisfied);
            // If no new conditions could be satisfied, stop looping.
        } while (!satisfied.isEmpty());

        return conditions;
    }

    Conditions gatherBooleanDataOfServer() {
        Conditions conditions = new Conditions();

        List<DataProvider<Boolean>> unsatisfiedProviders = new ArrayList<>(dataProviders.getServerMethodsByType(Boolean.class));
        Set<DataProvider<Boolean>> satisfied;

        // Method parameters abstracted away so that same method can be used for all parameter types
        // Same with Method result store transaction creation
        Function<MethodWrapper<Boolean>, Callable<Boolean>> methodCaller = method -> () -> method.callMethod(extension, Parameters.server(serverUUID));
        BiFunction<MethodWrapper<Boolean>, Boolean, Transaction> storeTransactionCreator = (method, result) -> new StoreServerBooleanResultTransaction(pluginName, serverUUID, method.getMethodName(), result);

        do {
            // Loop through all unsatisfied providers to see if more conditions are satisfied
            satisfied = attemptToSatisfyMoreConditionsAndStoreResults(methodCaller, storeTransactionCreator, conditions, unsatisfiedProviders);
            // Remove now satisfied Providers so that they are not called again
            unsatisfiedProviders.removeAll(satisfied);
            // If no new conditions could be satisfied, stop looping.
        } while (!satisfied.isEmpty());

        return conditions;
    }

    private Set<DataProvider<Boolean>> attemptToSatisfyMoreConditionsAndStoreResults(
            Function<MethodWrapper<Boolean>, Callable<Boolean>> methodCaller,
            BiFunction<MethodWrapper<Boolean>, Boolean, Transaction> storeTransactionCreator,
            Conditions conditions, List<DataProvider<Boolean>> unsatisfiedProviders
    ) {
        Set<DataProvider<Boolean>> satisfied = new HashSet<>();
        for (DataProvider<Boolean> booleanProvider : unsatisfiedProviders) {
            ProviderInformation information = booleanProvider.getProviderInformation();

            Optional<String> condition = information.getCondition();
            if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
                // Condition required by the BooleanProvider is not satisfied
                continue;
            }


            String providedCondition = information.getProvidedCondition();
            MethodWrapper<Boolean> method = booleanProvider.getMethod();
            Boolean result = getMethodResult(methodCaller.apply(method), method);
            if (result == null) {
                // Error during method call
                satisfied.add(booleanProvider); // Prevents further attempts to call this provider for this player.
                continue;
            }

            if (providedCondition != null) {
                if (Boolean.TRUE.equals(result)) {
                    // The condition was fulfilled (true) for this player.
                    conditions.conditionFulfilled(providedCondition);
                } else {
                    // The negated condition was fulfilled (false) for this player.
                    conditions.conditionFulfilled("not_" + providedCondition);
                }
            }

            satisfied.add(booleanProvider); // Prevents further attempts to call this provider for this player.
            database.executeTransaction(new StoreIconTransaction(information.getIcon()));
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