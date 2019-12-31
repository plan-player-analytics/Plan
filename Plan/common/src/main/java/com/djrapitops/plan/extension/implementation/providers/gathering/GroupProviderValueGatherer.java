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
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerGroupsResultTransaction;
import com.djrapitops.plan.storage.database.Database;

import java.util.Optional;
import java.util.UUID;

/**
 * Gathers GroupProvider method data.
 *
 * @author Rsl1122
 */
class GroupProviderValueGatherer {

    private final String pluginName;
    private final DataExtension extension;
    private final UUID serverUUID;

    private final Database database;
    private final DataProviders dataProviders;

    GroupProviderValueGatherer(
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

    void gatherGroupDataOfPlayer(UUID playerUUID, String playerName, Conditions conditions) {
        for (DataProvider<String[]> groupProvider : dataProviders.getPlayerMethodsByType(String[].class)) {
            gatherGroupDataOfProvider(playerUUID, playerName, conditions, groupProvider);
        }
    }

    private void gatherGroupDataOfProvider(
            UUID playerUUID, String playerName,
            Conditions conditions,
            DataProvider<String[]> groupProvider
    ) {
        ProviderInformation providerInformation = groupProvider.getProviderInformation();
        Optional<String> condition = providerInformation.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        MethodWrapper<String[]> method = groupProvider.getMethod();
        try {
            String[] result = method.callMethod(extension, Parameters.player(playerUUID, playerName));
            if (result == null) {
                return; // Error during call
            }

            database.executeTransaction(new StoreIconTransaction(providerInformation.getIcon()));
            database.executeTransaction(new StoreProviderTransaction(groupProvider, serverUUID));
            database.executeTransaction(new StorePlayerGroupsResultTransaction(pluginName, serverUUID, method.getMethodName(), playerUUID, result));
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
            throw new DataExtensionMethodCallException(e, pluginName, method);
        }
    }
}