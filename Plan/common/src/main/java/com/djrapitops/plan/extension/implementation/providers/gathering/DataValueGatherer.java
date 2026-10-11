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

import com.djrapitops.plan.component.Component;
import com.djrapitops.plan.component.ComponentOperation;
import com.djrapitops.plan.component.ComponentSvc;
import com.djrapitops.plan.exceptions.DataExtensionMethodCallException;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.extractor.ExtensionMethods;
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
import com.djrapitops.plan.extension.implementation.storage.transactions.results.*;
import com.djrapitops.plan.extension.statistic.Statistics;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.function.TriFunction;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;

/**
 * Object that can be called to place data about players to the database.
 *
 * @author AuroraLS3
 */
public class DataValueGatherer {

    private final CallEvents[] callEvents;
    private final ExtensionWrapper extension;
    private final DBSystem dbSystem;
    private final ExtensionMetadataStorage extensionMetadataStorage;
    private final ComponentSvc componentService;
    private final ServerInfo serverInfo;
    private final ErrorLogger errorLogger;

    private final Set<ExtensionMethod> brokenMethods;

    public DataValueGatherer(
            ExtensionWrapper extension,
            DBSystem dbSystem,
            ExtensionMetadataStorage extensionMetadataStorage,
            ComponentSvc componentService,
            ServerInfo serverInfo,
            ErrorLogger errorLogger
    ) {
        this.callEvents = extension.getCallEvents();
        this.extension = extension;
        this.dbSystem = dbSystem;
        this.extensionMetadataStorage = extensionMetadataStorage;
        this.componentService = componentService;
        this.serverInfo = serverInfo;
        this.errorLogger = errorLogger;

        this.brokenMethods = new HashSet<>();
    }

    public ExtensionWrapper getExtension() {
        return extension;
    }

    public boolean shouldSkipEvent(CallEvents event) {
        if (event == CallEvents.MANUAL) {
            return false;
        }
        return !event.isIn(callEvents);
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

    private void addValuesToBuilder(ExtensionDataBuilder b, ExtensionMethods methods, Parameters parameters) {
        addToBuilder(b, Boolean.class, this::tryToBuildBoolean, methods.getBooleanProviders(), parameters);
        addToBuilder(b, Double.class, this::tryToBuildDouble, methods.getDoubleProviders(), parameters);
        addToBuilder(b, Double.class, this::tryToBuildPercentage, methods.getPercentageProviders(), parameters);
        addToBuilder(b, Long.class, this::tryToBuildNumber, methods.getNumberProviders(), parameters);
        addToBuilder(b, String.class, this::tryToBuildString, methods.getStringProviders(), parameters);
        addToBuilder(b, Component.class, this::tryToBuildComponent, methods.getComponentProviders(), parameters);
        addToBuilder(b, String[].class, this::tryToBuildGroups, methods.getGroupProviders(), parameters);
        addToBuilder(b, Table.class, this::tryToBuildTable, methods.getTableProviders(), parameters);
        addToBuilder(b, Statistics.class, this::tryToBuildStatistics, methods.getStatisticsProviders(), parameters);

        for (ExtensionMethod provider : methods.getDataBuilderProviders()) {
            if (brokenMethods.contains(provider)) continue;
            addDataFromAnotherBuilder(b, parameters, provider);
        }
    }

    private <T> void addToBuilder(
            ExtensionDataBuilder builder,
            Class<T> type,
            TriFunction<ExtensionDataBuilder, Parameters, ExtensionMethod, DataValue<T>> buildMethod,
            List<ExtensionMethod> providers,
            Parameters parameters
    ) {
        for (ExtensionMethod provider : providers) {
            if (brokenMethods.contains(provider)) continue;
            builder.addValue(type, buildMethod.apply(builder, parameters, provider));
        }
    }

    private DataValue<Table> tryToBuildTable(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        TableProvider annotation = provider.getExistingAnnotation(TableProvider.class);
        try {
            return dataBuilder.valueBuilder(provider.getMethodName())
                    .methodName(provider)
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildTable(() -> callMethod(provider, parameters, Table.class), annotation.tableColor());
        } catch (IllegalArgumentException e) {
            logFailure(e, getPluginName(), provider.getMethodName());
            return null;
        }
    }

    private DataValue<Statistics> tryToBuildStatistics(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        StatisticsProvider annotation = provider.getExistingAnnotation(StatisticsProvider.class);
        try {
            return dataBuilder.valueBuilder(provider.getMethodName())
                    .methodName(provider)
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .icon(Icon.called(annotation.iconName()).of(annotation.iconFamily()).build())
                    .buildStatistics(() -> callMethod(provider, parameters, Statistics.class), annotation.tableColor());
        } catch (IllegalArgumentException e) {
            logFailure(e, getPluginName(), provider.getMethodName());
            return null;
        }
    }

    private DataValue<String[]> tryToBuildGroups(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        GroupProvider annotation = provider.getExistingAnnotation(GroupProvider.class);
        try {
            return dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.groupColor())
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildGroup(() -> callMethod(provider, parameters, String[].class));
        } catch (IllegalArgumentException e) {
            logFailure(e, getPluginName(), provider.getMethodName());
            return null;
        }
    }

    private DataValue<String> tryToBuildString(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        StringProvider annotation = provider.getExistingAnnotation(StringProvider.class);
        try {
            return dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .showAsPlayerPageLink(annotation)
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildString(() -> callMethod(provider, parameters, String.class));
        } catch (IllegalArgumentException e) {
            logFailure(e, getPluginName(), provider.getMethodName());
            return null;
        }
    }

    private DataValue<Component> tryToBuildComponent(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        ComponentProvider annotation = provider.getExistingAnnotation(ComponentProvider.class);
        try {
            return dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildComponent(() -> callMethod(provider, parameters, Component.class));
        } catch (IllegalArgumentException e) {
            logFailure(e, getPluginName(), provider.getMethodName());
            return null;
        }
    }

    private DataValue<Long> tryToBuildNumber(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        NumberProvider annotation = provider.getExistingAnnotation(NumberProvider.class);
        try {
            return dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .format(annotation.format())
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildNumber(() -> callMethod(provider, parameters, Long.class));
        } catch (IllegalArgumentException e) {
            logFailure(e, getPluginName(), provider.getMethodName());
            return null;
        }
    }

    private DataValue<Double> tryToBuildPercentage(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        PercentageProvider annotation = provider.getExistingAnnotation(PercentageProvider.class);
        try {
            return dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildPercentage(() -> callMethod(provider, parameters, Double.class));
        } catch (IllegalArgumentException e) {
            logFailure(e, getPluginName(), provider.getMethodName());
            return null;
        }
    }

    private DataValue<Double> tryToBuildDouble(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        DoubleProvider annotation = provider.getExistingAnnotation(DoubleProvider.class);
        try {
            return dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildDouble(() -> callMethod(provider, parameters, Double.class));
        } catch (IllegalArgumentException e) {
            logFailure(e, getPluginName(), provider.getMethodName());
            return null;
        }
    }

    private DataValue<Boolean> tryToBuildBoolean(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        BooleanProvider annotation = provider.getExistingAnnotation(BooleanProvider.class);
        try {
            return dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .hideFromUsers(annotation)
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildBooleanProvidingCondition(() -> callMethod(provider, parameters, Boolean.class), annotation.conditionName());
        } catch (IllegalArgumentException e) {
            logFailure(e, getPluginName(), provider.getMethodName());
            return null;
        }
    }

    private void addDataFromAnotherBuilder(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        try {
            ExtensionDataBuilder providedBuilder = callMethod(provider, parameters, ExtensionDataBuilder.class);
            dataBuilder.addAll(providedBuilder);
        } catch (DataExtensionMethodCallException methodError) {
            logFailure(methodError);
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError unexpectedError) {
            logFailure(unexpectedError);
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
        Conditions conditions = new Conditions();
        for (ExtDataBuilder.ClassValuePair pair : dataBuilder.getValues()) {
            try {
                valueFromPair(pair, Boolean.class, BooleanDataValue.class).ifPresent(data ->
                        storePlayerBoolean(parameters, conditions, data));
                valueFromPair(pair, Long.class, NumberDataValue.class).ifPresent(data ->
                        storeValue(parameters, conditions, data, StorePlayerNumberResultTransaction::new));
                valueFromPair(pair, Double.class, DoubleDataValue.class).ifPresent(data ->
                        storeValue(parameters, conditions, data, StorePlayerDoubleResultTransaction::new));
                valueFromPair(pair, String.class, StringDataValue.class).ifPresent(data ->
                        storeValue(parameters, conditions, data, StorePlayerStringResultTransaction::new));
                valueFromPair(pair, Component.class, ComponentDataValue.class).ifPresent(data ->
                        storePlayerComponent(parameters, conditions, data));
                valueFromPair(pair, String[].class, GroupsDataValue.class).ifPresent(data ->
                        storeValue(parameters, conditions, data, StorePlayerGroupsResultTransaction::new));
                valueFromPair(pair, Table.class, TableDataValue.class).ifPresent(data ->
                        storePlayerTable(parameters, conditions, data));
                valueFromPair(pair, Statistics.class, StatisticsDataValue.class).ifPresent(data ->
                        storePlayerStatistics(parameters, conditions, data));
            } catch (DataExtensionMethodCallException methodError) {
                logFailure(methodError);
            } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError unexpectedError) {
                logFailure(unexpectedError);
            }
        }
    }

    private void gather(Parameters parameters, ExtDataBuilder dataBuilder) {
        Conditions conditions = new Conditions();
        for (ExtDataBuilder.ClassValuePair pair : dataBuilder.getValues()) {
            try {
                valueFromPair(pair, Boolean.class, BooleanDataValue.class).ifPresent(data ->
                        storeBoolean(parameters, conditions, data));
                valueFromPair(pair, Long.class, NumberDataValue.class).ifPresent(data ->
                        storeValue(parameters, conditions, data, StoreServerNumberResultTransaction::new));
                valueFromPair(pair, Double.class, DoubleDataValue.class).ifPresent(data ->
                        storeValue(parameters, conditions, data, StoreServerDoubleResultTransaction::new));
                valueFromPair(pair, String.class, StringDataValue.class).ifPresent(data ->
                        storeValue(parameters, conditions, data, StoreServerStringResultTransaction::new));
                valueFromPair(pair, Component.class, ComponentDataValue.class).ifPresent(data ->
                        storeComponent(parameters, conditions, data));
                valueFromPair(pair, Table.class, TableDataValue.class).ifPresent(data ->
                        storeTable(parameters, conditions, data));
                valueFromPair(pair, Statistics.class, StatisticsDataValue.class).ifPresent(data ->
                        storeStatistics(parameters, conditions, data));
            } catch (DataExtensionMethodCallException methodError) {
                logFailure(methodError);
            } catch (RejectedExecutionException ignore) {
                // Processing or Database has shut down, which can be ignored
            } catch (Exception | ExceptionInInitializerError | NoClassDefFoundError | NoSuchFieldError |
                     NoSuchMethodError unexpectedError) {
                logFailure(unexpectedError);
            }
        }
    }

    private <T extends BuiltDataValue<V>, V> Optional<T> valueFromPair(ExtDataBuilder.ClassValuePair pair, Class<V> type, Class<T> valueWrapper) {
        return pair.getValue(type).flatMap(data -> data.getMetadata(valueWrapper));
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

    private String getComponentAsJson(Component component) {
        if (component == null) return null;

        String json = componentService.convert(component, ComponentOperation.JSON);
        if (json.length() > ComponentDataValue.MAX_LENGTH) {
            json = "{\"text\":\"<Component too long>\"}";
        }
        if (Strings.CI.containsAny(json, "javascript", "clickEvent", "hoverEvent", "open_url", "copy_to_clipboard", "\"action\"", "&#", "\0", "\\")) {
            json = "{\"text\":\"<Component contained disallowed words or characters>\"}";
        }
        return json;
    }

    private void storeBoolean(Parameters parameters, Conditions conditions, BooleanDataValue data) {
        var information = data.getInformation();
        var value = getValue(conditions, data, information);
        if (value == null) return;
        if (value) {
            conditions.conditionFulfilled(information.getProvidedCondition());
        } else {
            conditions.conditionFulfilled("not_" + information.getProvidedCondition());
        }

        Database db = dbSystem.getDatabase();
        extensionMetadataStorage.storeProvider(information, parameters);
        db.executeTransaction(new StoreServerBooleanResultTransaction(information, parameters, value));
    }

    private void storeComponent(Parameters parameters, Conditions conditions, ComponentDataValue data) {
        var information = data.getInformation();
        var value = getComponentAsJson(getValue(conditions, data, information));
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        extensionMetadataStorage.storeProvider(information, parameters);
        db.executeTransaction(new StoreServerStringResultTransaction(information, parameters, value));
    }

    private void storeTable(Parameters parameters, Conditions conditions, TableDataValue data) {
        var information = data.getInformation();
        var value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        extensionMetadataStorage.storeTableProvider(information, parameters, value);
        db.executeTransaction(new StoreServerTableResultTransaction(information, parameters, value));
    }

    private void storeStatistics(Parameters parameters, Conditions conditions, StatisticsDataValue data) {
        var information = data.getInformation();
        var value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        extensionMetadataStorage.storeProvider(information, parameters);
        db.executeTransaction(new StoreServerStatisticsResultTransaction(information, parameters, value));
    }

    private void storePlayerBoolean(Parameters parameters, Conditions conditions, BooleanDataValue data) {
        var information = data.getInformation();
        var value = getValue(conditions, data, information);
        if (value == null) return;
        if (value) {
            conditions.conditionFulfilled(information.getProvidedCondition());
        } else {
            conditions.conditionFulfilled("not_" + information.getProvidedCondition());
        }

        Database db = dbSystem.getDatabase();
        extensionMetadataStorage.storeProvider(information, parameters);
        db.executeTransaction(new StorePlayerBooleanResultTransaction(information, parameters, value));
    }

    public <T> void storeValue(
            Parameters parameters, Conditions conditions, BuiltDataValue<T> data,
            TriFunction<ProviderInformation, Parameters, T, Transaction> transactionConstructor
    ) {
        var information = data.getInformation();
        var value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        extensionMetadataStorage.storeProvider(information, parameters);
        db.executeTransaction(transactionConstructor.apply(information, parameters, value));
    }

    private void storePlayerComponent(Parameters parameters, Conditions conditions, ComponentDataValue data) {
        var information = data.getInformation();
        var value = getComponentAsJson(getValue(conditions, data, information));
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        extensionMetadataStorage.storeProvider(information, parameters);
        db.executeTransaction(new StorePlayerStringResultTransaction(information, parameters, value));
    }

    private void storePlayerTable(Parameters parameters, Conditions conditions, TableDataValue data) {
        var information = data.getInformation();
        var value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        extensionMetadataStorage.storeTableProvider(information, parameters, value);
        db.executeTransaction(new StorePlayerTableResultTransaction(information, parameters, value));
    }

    private void storePlayerStatistics(Parameters parameters, Conditions conditions, StatisticsDataValue data) {
        var information = data.getInformation();
        var value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        extensionMetadataStorage.storeProvider(information, parameters);
        db.executeTransaction(new StorePlayerStatisticsResultTransaction(information, parameters, value));
    }
}