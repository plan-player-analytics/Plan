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
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.extractor.ExtensionMethods;
import com.djrapitops.plan.extension.extractor.dataprovider.AnnotationDataProvider;
import com.djrapitops.plan.extension.extractor.dataprovider.DataBuilderDataProvider;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.builder.*;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.StorePluginTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreTabInformationTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreTableProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.*;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Object that can be called to place data about players to the database.
 *
 * @author AuroraLS3
 */
public class DataValueGatherer {

    private final CallEvents[] callEvents;
    private final ExtensionWrapper extension;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ErrorLogger errorLogger;

    private final Set<ExtensionMethod> brokenMethods;

    public DataValueGatherer(
            ExtensionWrapper extension,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            ErrorLogger errorLogger
    ) {
        this.callEvents = extension.getCallEvents();
        this.extension = extension;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.errorLogger = errorLogger;

        this.brokenMethods = new HashSet<>();
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
        return extension.getPluginName();
    }

    public void storeExtensionInformation() {
        String pluginName = extension.getPluginName();
        Icon pluginIcon = extension.getPluginIcon();

        long time = System.currentTimeMillis();
        ServerUUID serverUUID = serverInfo.getServerUUID();

        Database database = dbSystem.getDatabase();
        database.executeTransaction(new StoreIconTransaction(pluginIcon));
        database.executeTransaction(new StorePluginTransaction(pluginName, time, serverUUID, pluginIcon));
        for (TabInformation tab : extension.getPluginTabs()) {
            database.executeTransaction(new StoreIconTransaction(tab.getTabIcon()));
            database.executeTransaction(new StoreTabInformationTransaction(pluginName, serverUUID, tab));
        }

        database.executeTransaction(new RemoveInvalidResultsTransaction(pluginName, serverUUID, extension.getInvalidatedMethods()));
    }

    private void addValuesToBuilder(ExtensionDataBuilder dataBuilder, ExtensionMethods methods, Parameters parameters) {
        for (AnnotationDataProvider<?, ?, ?> extractor : methods.getProviders()) {
            ExtensionMethod method = extractor.getExtensionMethod();
            if (brokenMethods.contains(method)) continue;
            tryToAdd(extractor, dataBuilder, parameters, method);
        }
    }

    private <D> void tryToAdd(AnnotationDataProvider<?, ?, D> extractor, ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        if (extractor instanceof DataBuilderDataProvider) {
            try {
                ExtensionDataBuilder providedBuilder = callMethod(provider, parameters, ExtensionDataBuilder.class);
                dataBuilder.addAll(providedBuilder);
            } catch (DataExtensionMethodCallException methodError) {
                logFailure(methodError);
            } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError unexpectedError) {
                logFailure(unexpectedError);
            }
        }

        try {
            ValueBuilder valueBuilder = extractor.getValueBuilder(dataBuilder);
            DataValue<D> dataValue = extractor.addDataValueToBuilder(
                    valueBuilder,
                    () -> callMethod(provider, parameters, extractor.getDataType())
            );
            dataBuilder.addValue(extractor.getDataType(), dataValue);
        } catch (IllegalArgumentException e) {
            logFailure(e, getPluginName(), provider.getMethodName());
        }
    }

    private <T> T callMethod(ExtensionMethod provider, Parameters params, Class<T> returnType) {
        try {
            return new MethodWrapper<>(provider.getMethod(), returnType)
                    .callMethod(extension.getExtension(), params);
        } catch (DataExtensionMethodCallException e) {
            brokenMethods.add(provider);
            throw e;
        }
    }

    public void updateValues(UUID playerUUID, String playerName) {
        try {
            tryToUpdateValues(playerUUID, playerName);
        } catch (RejectedExecutionException ignore) {
            // Database has shut down
        }
    }

    private void tryToUpdateValues(UUID playerUUID, String playerName) {
        Parameters parameters = Parameters.player(serverInfo.getServerUUID(), playerUUID, playerName);
        ExtensionDataBuilder dataBuilder = extension.getExtension().newExtensionDataBuilder();

        addValuesToBuilder(dataBuilder, extension.getMethods().get(ExtensionMethod.ParameterType.PLAYER_STRING), parameters);
        addValuesToBuilder(dataBuilder, extension.getMethods().get(ExtensionMethod.ParameterType.PLAYER_UUID), parameters);

        gatherPlayer(parameters, (ExtDataBuilder) dataBuilder);

        dbSystem.getDatabase().executeTransaction(new RemoveInvalidResultsTransaction(extension.getPluginName(), serverInfo.getServerUUID(), ((ExtDataBuilder) dataBuilder).getInvalidatedValues()));
    }

    public void updateValues() {
        try {
            tryToUpdateValues();
        } catch (RejectedExecutionException ignore) {
            // Database has shut down
        }
    }

    private void tryToUpdateValues() {
        Parameters parameters = Parameters.server(serverInfo.getServerUUID());
        ExtensionDataBuilder dataBuilder = extension.getExtension().newExtensionDataBuilder();

        addValuesToBuilder(dataBuilder, extension.getMethods().get(ExtensionMethod.ParameterType.SERVER_NONE), parameters);

        gather(parameters, (ExtDataBuilder) dataBuilder);
    }


    private void gatherPlayer(Parameters parameters, ExtDataBuilder dataBuilder) {
        gather(
                parameters,
                dataBuilder,
                StorePlayerBooleanResultTransaction::new,
                StorePlayerNumberResultTransaction::new,
                StorePlayerDoubleResultTransaction::new,
                StorePlayerStringResultTransaction::new,
                StorePlayerTableResultTransaction::new,
                StorePlayerGroupsResultTransaction::new
        );

    }

    private void gather(Parameters parameters, ExtDataBuilder dataBuilder) {
        gather(
                parameters,
                dataBuilder,
                StoreServerBooleanResultTransaction::new,
                StoreServerNumberResultTransaction::new,
                StoreServerDoubleResultTransaction::new,
                StoreServerStringResultTransaction::new,
                StoreServerTableResultTransaction::new,
                null
        );
    }

    private void gather(
            Parameters parameters,
            ExtDataBuilder dataBuilder,
            TransactionConstructor<Boolean> booleanTransaction,
            TransactionConstructor<Long> numberTransaction,
            TransactionConstructor<Double> doubleTransaction,
            TransactionConstructor<String> stringTransaction,
            TransactionConstructor<Table> tableTransaction,
            TransactionConstructor<String[]> groupTransaction
            ) {
        Conditions conditions = new Conditions();
        for (ExtDataBuilder.ClassValuePair pair : dataBuilder.getValues()) {
            try {
                pair.getValue(Boolean.class).flatMap(data -> data.getMetadata(BooleanDataValue.class))
                        .ifPresent(data -> storeBoolean(parameters, conditions, data, booleanTransaction));
                pair.getValue(Long.class).flatMap(data -> data.getMetadata(NumberDataValue.class))
                        .ifPresent(data -> store(parameters, conditions, data, numberTransaction));
                pair.getValue(Double.class).flatMap(data -> data.getMetadata(DoubleDataValue.class))
                        .ifPresent(data -> store(parameters, conditions, data, doubleTransaction));
                pair.getValue(String.class).flatMap(data -> data.getMetadata(StringDataValue.class))
                        .ifPresent(data -> store(parameters, conditions, data, stringTransaction));
                pair.getValue(Table.class).flatMap(data -> data.getMetadata(TableDataValue.class))
                        .ifPresent(data -> storeTable(parameters, conditions, data, tableTransaction));
                if (groupTransaction != null) {
                    pair.getValue(String[].class).flatMap(data -> data.getMetadata(GroupsDataValue.class))
                            .ifPresent(data -> store(parameters, conditions, data, groupTransaction));
                }
            } catch (DataExtensionMethodCallException methodError) {
                logFailure(methodError);
            } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError unexpectedError) {
                logFailure(unexpectedError);
            }
        }
    }

    @FunctionalInterface
    private interface TransactionConstructor<T> {
        ThrowawayTransaction newInstance(ProviderInformation information, Parameters parameters, T data);
    }

    private void storeBoolean(Parameters parameters, Conditions conditions, BooleanDataValue data, TransactionConstructor<Boolean> constructor) {
        store(parameters, conditions, data, (information, value) -> {
            if (value) {
                conditions.conditionFulfilled(information.getProvidedCondition());
            } else {
                conditions.conditionFulfilled("not_" + information.getProvidedCondition());
            }
        }, constructor);
    }

    private <T> void store(
            Parameters parameters,
            Conditions conditions,
            DataValue<T> data,
            TransactionConstructor<T> constructor
    ) {
        store(parameters, conditions, data, (info, value) -> {}, constructor);
    }

    private <T> void store(
            Parameters parameters,
            Conditions conditions,
            DataValue<T> data,
            BiConsumer<ProviderInformation, T> valueConsumer,
            TransactionConstructor<T> constructor
    ) {
        store(conditions, data, valueConsumer, (information, value) -> Arrays.asList(
                new StoreIconTransaction(information.getIcon()),
                new StoreProviderTransaction(information, parameters),
                constructor.newInstance(information, parameters, value)
        ));
    }

    private void storeTable(
            Parameters parameters,
            Conditions conditions,
            TableDataValue dataValue,
            TransactionConstructor<Table> constructor
    ) {
        store(conditions, dataValue, (info, value) -> {}, (information, value) -> {
            List<ThrowawayTransaction> transactions = new ArrayList<>();
            for (Icon icon : value.getIcons()) {
                if (icon != null) transactions.add(new StoreIconTransaction(icon));
            }
            transactions.add(new StoreTableProviderTransaction(information, parameters, value));
            transactions.add(constructor.newInstance(information, parameters, value));
            return transactions;
        });
    }

    private <T> void store(
            Conditions conditions,
            DataValue<T> data,
            BiConsumer<ProviderInformation,T> valueConsumer,
            BiFunction<ProviderInformation, T, List<ThrowawayTransaction>> transactionFunction
    ) {
        ProviderInformation information = data.getInformation(ProviderInformation.class);
        if (information == null) return;

        T value = getValue(conditions, data, information);
        if (value == null) return;

        valueConsumer.accept(information, value);

        Database db = dbSystem.getDatabase();
        for (ThrowawayTransaction transaction : transactionFunction.apply(information, value)) {
            db.executeTransaction(transaction);
        }
    }

    private void logFailure(Throwable cause, String pluginName, String methodName) {
        ErrorContext.Builder context = ErrorContext.builder()
                .whatToDo(getWhatToDoMessage(pluginName))
                .related(pluginName)
                .related("Method:" + methodName);
        errorLogger.warn(cause, context.build());
    }

    private String getWhatToDoMessage(String pluginName) {
        return "Report and/or disable " + pluginName + " extension in the Plan config.";
    }

    private void logFailure(DataExtensionMethodCallException methodCallFailed) {
        ErrorContext.Builder context = ErrorContext.builder()
                .whatToDo(getWhatToDoMessage(methodCallFailed.getPluginName()))
                .related(methodCallFailed.getPluginName())
                .related("Method:" + methodCallFailed.getMethodName().orElse("-"));
        errorLogger.warn(methodCallFailed, context.build());
    }

    private void logFailure(Throwable unexpectedError) {
        ErrorContext.Builder context = ErrorContext.builder()
                .whatToDo(getWhatToDoMessage(extension.getPluginName()))
                .related(extension.getPluginName());
        errorLogger.warn(unexpectedError, context.build());
    }

    private <T> T getValue(Conditions conditions, DataValue<T> data, ProviderInformation information) {
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return null;
        }
        return data.getValue(); // can be null, can throw
    }

}