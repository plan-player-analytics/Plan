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
import com.djrapitops.plan.extension.icon.Color;
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
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import org.apache.commons.lang3.Strings;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
    private final ComponentSvc componentService;
    private final ServerInfo serverInfo;
    private final ErrorLogger errorLogger;

    private final Set<ExtensionMethod> brokenMethods;

    public DataValueGatherer(
            ExtensionWrapper extension,
            DBSystem dbSystem,
            ComponentSvc componentService,
            ServerInfo serverInfo,
            ErrorLogger errorLogger
    ) {
        this.callEvents = extension.getCallEvents();
        this.extension = extension;
        this.dbSystem = dbSystem;
        this.componentService = componentService;
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
        for (ExtensionMethod provider : methods.getBooleanProviders()) {
            if (brokenMethods.contains(provider)) continue;
            dataBuilder.addValue(Boolean.class, tryToBuildBoolean(dataBuilder, parameters, provider));
        }
        for (ExtensionMethod provider : methods.getDoubleProviders()) {
            if (brokenMethods.contains(provider)) continue;
            dataBuilder.addValue(Double.class, tryToBuildDouble(dataBuilder, parameters, provider));
        }
        for (ExtensionMethod provider : methods.getPercentageProviders()) {
            if (brokenMethods.contains(provider)) continue;
            dataBuilder.addValue(Double.class, tryToBuildPercentage(dataBuilder, parameters, provider));
        }
        for (ExtensionMethod provider : methods.getNumberProviders()) {
            if (brokenMethods.contains(provider)) continue;
            dataBuilder.addValue(Long.class, tryToBuildNumber(dataBuilder, parameters, provider));
        }
        for (ExtensionMethod provider : methods.getStringProviders()) {
            if (brokenMethods.contains(provider)) continue;
            dataBuilder.addValue(String.class, tryToBuildString(dataBuilder, parameters, provider));
        }
        for (ExtensionMethod provider : methods.getComponentProviders()) {
            if (brokenMethods.contains(provider)) continue;
            dataBuilder.addValue(Component.class, tryToBuildComponent(dataBuilder, parameters, provider));
        }
        for (ExtensionMethod provider : methods.getGroupProviders()) {
            if (brokenMethods.contains(provider)) continue;
            dataBuilder.addValue(String[].class, tryToBuildGroups(dataBuilder, parameters, provider));
        }
        for (ExtensionMethod provider : methods.getTableProviders()) {
            if (brokenMethods.contains(provider)) continue;
            dataBuilder.addValue(Table.class, tryToBuildTable(dataBuilder, parameters, provider));
        }
        addValuesToBuilder2(dataBuilder, methods, parameters);
    }

    // TODO refactor to reduce cyclomatic complexity of the calling method
    private void addValuesToBuilder2(ExtensionDataBuilder dataBuilder, ExtensionMethods methods, Parameters parameters) {
        for (ExtensionMethod provider : methods.getDataBuilderProviders()) {
            if (brokenMethods.contains(provider)) continue;
            addDataFromAnotherBuilder(dataBuilder, parameters, provider);
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

    private DataValue<String[]> tryToBuildGroups(ExtensionDataBuilder dataBuilder, Parameters parameters, ExtensionMethod provider) {
        GroupProvider annotation = provider.getExistingAnnotation(GroupProvider.class);
        try {
            return dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), Color.NONE)
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
                pair.getValue(Boolean.class).flatMap(data -> data.getMetadata(BooleanDataValue.class))
                        .ifPresent(data -> storePlayerBoolean(parameters, conditions, data));
                pair.getValue(Long.class).flatMap(data -> data.getMetadata(NumberDataValue.class))
                        .ifPresent(data -> storePlayerNumber(parameters, conditions, data));
                pair.getValue(Double.class).flatMap(data -> data.getMetadata(DoubleDataValue.class))
                        .ifPresent(data -> storePlayerDouble(parameters, conditions, data));
                pair.getValue(String.class).flatMap(data -> data.getMetadata(StringDataValue.class))
                        .ifPresent(data -> storePlayerString(parameters, conditions, data));
                pair.getValue(Component.class).flatMap(data -> data.getMetadata(ComponentDataValue.class))
                        .ifPresent(data -> storePlayerComponent(parameters, conditions, data));
                pair.getValue(String[].class).flatMap(data -> data.getMetadata(GroupsDataValue.class))
                        .ifPresent(data -> storePlayerGroups(parameters, conditions, data));
                pair.getValue(Table.class).flatMap(data -> data.getMetadata(TableDataValue.class))
                        .ifPresent(data -> storePlayerTable(parameters, conditions, data));
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
                pair.getValue(Boolean.class).flatMap(data -> data.getMetadata(BooleanDataValue.class))
                        .ifPresent(data -> storeBoolean(parameters, conditions, data));
                pair.getValue(Long.class).flatMap(data -> data.getMetadata(NumberDataValue.class))
                        .ifPresent(data -> storeNumber(parameters, conditions, data));
                pair.getValue(Double.class).flatMap(data -> data.getMetadata(DoubleDataValue.class))
                        .ifPresent(data -> storeDouble(parameters, conditions, data));
                pair.getValue(String.class).flatMap(data -> data.getMetadata(StringDataValue.class))
                        .ifPresent(data -> storeString(parameters, conditions, data));
                pair.getValue(Component.class).flatMap(data -> data.getMetadata(ComponentDataValue.class))
                        .ifPresent(data -> storeComponent(parameters, conditions, data));
                pair.getValue(Table.class).flatMap(data -> data.getMetadata(TableDataValue.class))
                        .ifPresent(data -> storeTable(parameters, conditions, data));
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
        ProviderInformation information = data.getInformation();
        Boolean value = getValue(conditions, data, information);
        if (value == null) return;
        if (value) {
            conditions.conditionFulfilled(information.getProvidedCondition());
        } else {
            conditions.conditionFulfilled("not_" + information.getProvidedCondition());
        }

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StoreServerBooleanResultTransaction(information, parameters, value));
    }

    private void storeNumber(Parameters parameters, Conditions conditions, NumberDataValue data) {
        ProviderInformation information = data.getInformation();
        Long value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StoreServerNumberResultTransaction(information, parameters, value));
    }


    private void storeDouble(Parameters parameters, Conditions conditions, DoubleDataValue data) {
        ProviderInformation information = data.getInformation();
        Double value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StoreServerDoubleResultTransaction(information, parameters, value));
    }

    private void storeString(Parameters parameters, Conditions conditions, StringDataValue data) {
        ProviderInformation information = data.getInformation();
        String value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StoreServerStringResultTransaction(information, parameters, value));
    }

    private void storeComponent(Parameters parameters, Conditions conditions, ComponentDataValue data) {
        ProviderInformation information = data.getInformation();
        String value = getComponentAsJson(getValue(conditions, data, information));
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StoreServerStringResultTransaction(information, parameters, value));
    }

    private void storeTable(Parameters parameters, Conditions conditions, TableDataValue data) {
        ProviderInformation information = data.getInformation();
        Table value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        for (Icon icon : value.getIcons()) {
            if (icon != null) db.executeTransaction(new StoreIconTransaction(icon));
        }
        db.executeTransaction(new StoreTableProviderTransaction(information, parameters, value));
        db.executeTransaction(new StoreServerTableResultTransaction(information, parameters, value));
    }

    private void storePlayerBoolean(Parameters parameters, Conditions conditions, BooleanDataValue data) {
        ProviderInformation information = data.getInformation();
        Boolean value = getValue(conditions, data, information);
        if (value == null) return;
        if (value) {
            conditions.conditionFulfilled(information.getProvidedCondition());
        } else {
            conditions.conditionFulfilled("not_" + information.getProvidedCondition());
        }

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StorePlayerBooleanResultTransaction(information, parameters, value));
    }

    private void storePlayerNumber(Parameters parameters, Conditions conditions, NumberDataValue data) {
        ProviderInformation information = data.getInformation();
        Long value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StorePlayerNumberResultTransaction(information, parameters, value));
    }

    private void storePlayerDouble(Parameters parameters, Conditions conditions, DoubleDataValue data) {
        ProviderInformation information = data.getInformation();
        Double value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StorePlayerDoubleResultTransaction(information, parameters, value));
    }

    private void storePlayerString(Parameters parameters, Conditions conditions, StringDataValue data) {
        ProviderInformation information = data.getInformation();
        String value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StorePlayerStringResultTransaction(information, parameters, value));
    }

    private void storePlayerComponent(Parameters parameters, Conditions conditions, ComponentDataValue data) {
        ProviderInformation information = data.getInformation();
        String value = getComponentAsJson(getValue(conditions, data, information));
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StorePlayerStringResultTransaction(information, parameters, value));
    }

    private void storePlayerGroups(Parameters parameters, Conditions conditions, GroupsDataValue data) {
        ProviderInformation information = data.getInformation();
        String[] value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StorePlayerGroupsResultTransaction(information, parameters, value));
    }

    private void storePlayerTable(Parameters parameters, Conditions conditions, TableDataValue data) {
        ProviderInformation information = data.getInformation();
        Table value = getValue(conditions, data, information);
        if (value == null) return;

        Database db = dbSystem.getDatabase();
        for (Icon icon : value.getIcons()) {
            if (icon != null) db.executeTransaction(new StoreIconTransaction(icon));
        }
        db.executeTransaction(new StoreTableProviderTransaction(information, parameters, value));
        db.executeTransaction(new StorePlayerTableResultTransaction(information, parameters, value));
    }
}