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
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.DataProviderExtractor;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.extension.implementation.providers.DataProviders;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.StorePluginTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreTabInformationTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.RemoveInvalidResultsTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerNumberResultTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StoreServerNumberResultTransaction;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Object that can be called to place data about players to the database.
 *
 * @author Rsl1122
 */
public class ProviderValueGatherer {

    private final DataExtension extension;

    private final CallEvents[] callEvents;
    private final DataProviderExtractor extractor;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    private final DataProviders dataProviders;
    private final BooleanProviderValueGatherer booleanGatherer;
    private final NumberProviderValueGatherer numberGatherer;
    private final DoubleAndPercentageProviderValueGatherer doubleAndPercentageGatherer;
    private final StringProviderValueGatherer stringGatherer;
    private final TableProviderValueGatherer tableGatherer;
    private final GroupProviderValueGatherer groupGatherer;
    private Gatherer<Long> serverNumberGatherer;
    private Gatherer<Long> playerNumberGatherer;

    public ProviderValueGatherer(
            DataExtension extension,
            DataProviderExtractor extractor,
            DBSystem dbSystem,
            ServerInfo serverInfo
    ) {
        this.extension = extension;
        this.callEvents = this.extension.callExtensionMethodsOn();
        this.extractor = extractor;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;

        String pluginName = extractor.getPluginName();
        UUID serverUUID = serverInfo.getServerUUID();
        Database database = dbSystem.getDatabase();
        dataProviders = extractor.getProviders();
        booleanGatherer = new BooleanProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
        numberGatherer = new NumberProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
        doubleAndPercentageGatherer = new DoubleAndPercentageProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
        stringGatherer = new StringProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
        tableGatherer = new TableProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
        groupGatherer = new GroupProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );

        serverNumberGatherer = new Gatherer<>(
                Long.class,
                method -> method.callMethod(extension),
                StoreProviderTransaction::new,
                (provider, result) -> new StoreServerNumberResultTransaction(provider, serverUUID, result)
        );
    }

    public void disableMethodFromUse(MethodWrapper<?> method) {
        dataProviders.removeProviderWithMethod(method);
    }

    public boolean shouldSkipEvent(CallEvents event) {
        if (event == CallEvents.MANUAL) {
            return false;
        }
        for (CallEvents accepted : callEvents) {
            if (event == accepted) {
                return false;
            }
        }
        return true;
    }

    public String getPluginName() {
        return extractor.getPluginName();
    }

    public void storeExtensionInformation() {
        String pluginName = extractor.getPluginName();
        Icon pluginIcon = extractor.getPluginIcon();

        long time = System.currentTimeMillis();
        UUID serverUUID = serverInfo.getServerUUID();

        Database database = dbSystem.getDatabase();
        database.executeTransaction(new StoreIconTransaction(pluginIcon));
        database.executeTransaction(new StorePluginTransaction(pluginName, time, serverUUID, pluginIcon));
        for (TabInformation tab : extractor.getPluginTabs()) {
            database.executeTransaction(new StoreIconTransaction(tab.getTabIcon()));
            database.executeTransaction(new StoreTabInformationTransaction(pluginName, serverUUID, tab));
        }

        database.executeTransaction(new RemoveInvalidResultsTransaction(pluginName, serverUUID, extractor.getInvalidatedMethods()));
    }

    public void updateValues(UUID playerUUID, String playerName) {
        Conditions conditions = booleanGatherer.gatherBooleanDataOfPlayer(playerUUID, playerName);

        UUID serverUUID = serverInfo.getServerUUID();
        playerNumberGatherer = new Gatherer<>(
                Long.class, method -> method.callMethod(extension, playerUUID, playerName),
                StoreProviderTransaction::new,
                (provider, result) -> new StorePlayerNumberResultTransaction(provider, serverUUID, playerUUID, result)
        );
        playerNumberGatherer.gather(conditions);
        doubleAndPercentageGatherer.gatherDoubleDataOfPlayer(playerUUID, playerName, conditions);
        stringGatherer.gatherStringDataOfPlayer(playerUUID, playerName, conditions);
        tableGatherer.gatherTableDataOfPlayer(playerUUID, playerName, conditions);
        groupGatherer.gatherGroupDataOfPlayer(playerUUID, playerName, conditions);
    }

    public void updateValues() {
        Conditions conditions = booleanGatherer.gatherBooleanDataOfServer();
        numberGatherer.gatherNumberDataOfServer(conditions);
        doubleAndPercentageGatherer.gatherDoubleDataOfServer(conditions);
        stringGatherer.gatherStringDataOfServer(conditions);
        tableGatherer.gatherTableDataOfServer(conditions);
    }

    interface MethodCaller<T> extends Function<MethodWrapper<T>, T> {
        default T call(DataProvider<T> provider) {
            try {
                return apply(provider.getMethod());
            } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
                throw new DataExtensionMethodCallException(e, provider.getProviderInformation().getPluginName(), provider.getMethod());
            }
        }
    }

    interface ProviderTransactionConstructor<T> extends BiFunction<DataProvider<T>, UUID, Transaction> {
        default Transaction create(DataProvider<T> provider, UUID serverUUID) {
            return apply(provider, serverUUID);
        }
    }

    interface ResultTransactionConstructor<T> extends BiFunction<DataProvider<T>, T, Transaction> {
        default Transaction create(DataProvider<T> provider, T result) {
            return apply(provider, result);
        }
    }

    class Gatherer<T> {
        private final Class<T> type;
        private final MethodCaller<T> methodCaller;
        private final ProviderTransactionConstructor<T> providerTransactionConstructor;
        private final ResultTransactionConstructor<T> resultTransactionConstructor;

        public Gatherer(
                Class<T> type,
                MethodCaller<T> methodCaller,
                ProviderTransactionConstructor<T> providerTransactionConstructor,
                ResultTransactionConstructor<T> resultTransactionConstructor
        ) {
            this.type = type;
            this.methodCaller = methodCaller;
            this.providerTransactionConstructor = providerTransactionConstructor;
            this.resultTransactionConstructor = resultTransactionConstructor;
        }

        public void gather(Conditions conditions) {
            for (DataProvider<T> provider : dataProviders.getPlayerMethodsByType(type)) { // TODO work this out
                gather(conditions, provider);
            }
        }

        private void gather(Conditions conditions, DataProvider<T> provider) {
            ProviderInformation information = provider.getProviderInformation();
            if (information.getCondition().map(conditions::isNotFulfilled).orElse(false)) {
                return; // Condition not fulfilled
            }

            T result = methodCaller.call(provider);
            if (result == null) {
                return; // Error during method call
            }

            Database db = dbSystem.getDatabase();
            db.executeTransaction(new StoreIconTransaction(information.getIcon()));
            db.executeTransaction(providerTransactionConstructor.create(provider, serverInfo.getServerUUID()));
            db.executeTransaction(resultTransactionConstructor.create(provider, result));
        }
    }
}