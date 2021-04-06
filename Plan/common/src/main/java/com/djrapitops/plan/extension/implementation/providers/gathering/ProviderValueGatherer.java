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

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.extractor.ExtensionMethods;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.builder.*;
import com.djrapitops.plan.extension.implementation.providers.DataProviders;
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

import java.util.Optional;
import java.util.UUID;

/**
 * Object that can be called to place data about players to the database.
 *
 * @author AuroraLS3
 */
public class ProviderValueGatherer {

    private final CallEvents[] callEvents;
    private final ExtensionWrapper extensionWrapper;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    private final DataProviders dataProviders;

    public ProviderValueGatherer(
            ExtensionWrapper extension,
            DBSystem dbSystem,
            ServerInfo serverInfo
    ) {
        this.callEvents = extension.getCallEvents();
        this.extensionWrapper = extension;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;

        dataProviders = extension.getProviders();
    }

    public void disableMethodFromUse(MethodWrapper<?> method) {
        method.disable();
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
        return extensionWrapper.getPluginName();
    }

    public void storeExtensionInformation() {
        String pluginName = extensionWrapper.getPluginName();
        Icon pluginIcon = extensionWrapper.getPluginIcon();

        long time = System.currentTimeMillis();
        ServerUUID serverUUID = serverInfo.getServerUUID();

        Database database = dbSystem.getDatabase();
        database.executeTransaction(new StoreIconTransaction(pluginIcon));
        database.executeTransaction(new StorePluginTransaction(pluginName, time, serverUUID, pluginIcon));
        for (TabInformation tab : extensionWrapper.getPluginTabs()) {
            database.executeTransaction(new StoreIconTransaction(tab.getTabIcon()));
            database.executeTransaction(new StoreTabInformationTransaction(pluginName, serverUUID, tab));
        }

        database.executeTransaction(new RemoveInvalidResultsTransaction(pluginName, serverUUID, extensionWrapper.getInvalidatedMethods()));
    }

    private void addValuesToBuilder(ExtensionDataBuilder dataBuilder, ExtensionMethods methods, Parameters parameters) {
        for (ExtensionMethod provider : methods.getBooleanProviders()) {
            BooleanProvider annotation = provider.getExistingAnnotation(BooleanProvider.class);
            dataBuilder.addValue(Boolean.class, dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .hideFromUsers(annotation)
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildBooleanProvidingCondition(() -> callMethod(provider, parameters, Boolean.class), annotation.conditionName()));
        }
        for (ExtensionMethod provider : methods.getDoubleProviders()) {
            DoubleProvider annotation = provider.getExistingAnnotation(DoubleProvider.class);
            dataBuilder.addValue(Double.class, dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildDouble(() -> callMethod(provider, parameters, Double.class)));
        }
        for (ExtensionMethod provider : methods.getPercentageProviders()) {
            PercentageProvider annotation = provider.getExistingAnnotation(PercentageProvider.class);
            dataBuilder.addValue(Double.class, dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildPercentage(() -> callMethod(provider, parameters, Double.class)));
        }
        for (ExtensionMethod provider : methods.getNumberProviders()) {
            NumberProvider annotation = provider.getExistingAnnotation(NumberProvider.class);
            dataBuilder.addValue(Long.class, dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .format(annotation.format())
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildNumber(() -> callMethod(provider, parameters, Long.class)));
        }
        for (ExtensionMethod provider : methods.getStringProviders()) {
            StringProvider annotation = provider.getExistingAnnotation(StringProvider.class);
            dataBuilder.addValue(String.class, dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), annotation.iconColor())
                    .description(annotation.description())
                    .priority(annotation.priority())
                    .showInPlayerTable(annotation.showInPlayerTable())
                    .showAsPlayerPageLink(annotation)
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildString(() -> callMethod(provider, parameters, String.class)));
        }
        for (ExtensionMethod provider : methods.getGroupProviders()) {
            GroupProvider annotation = provider.getExistingAnnotation(GroupProvider.class);
            dataBuilder.addValue(String[].class, dataBuilder.valueBuilder(annotation.text())
                    .methodName(provider)
                    .icon(annotation.iconName(), annotation.iconFamily(), Color.NONE)
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildGroup(() -> callMethod(provider, parameters, String[].class)));
        }
        for (ExtensionMethod provider : methods.getTableProviders()) {
            TableProvider annotation = provider.getExistingAnnotation(TableProvider.class);
            dataBuilder.addValue(Table.class, dataBuilder.valueBuilder(provider.getMethodName())
                    .conditional(provider.getAnnotationOrNull(Conditional.class))
                    .showOnTab(provider.getAnnotationOrNull(Tab.class))
                    .buildTable(() -> callMethod(provider, parameters, Table.class), annotation.tableColor()));
        }
        for (ExtensionMethod provider : methods.getDataBuilderProviders()) {
            ExtensionDataBuilder providedBuilder = callMethod(provider, parameters, ExtensionDataBuilder.class);
            dataBuilder.addAll(providedBuilder);
        }
    }

    private <T> T callMethod(ExtensionMethod provider, Parameters params, Class<T> returnType) {
        try {
            return new MethodWrapper<>(provider.getMethod(), returnType)
                    .callMethod(extensionWrapper.getExtension(), params);
        } catch (IllegalArgumentException e) {
            return null; // TODO handle exceptions
        }
    }

    public void updateValues(UUID playerUUID, String playerName) {
        Parameters parameters = Parameters.player(serverInfo.getServerUUID(), playerUUID, playerName);
        ExtensionDataBuilder dataBuilder = extensionWrapper.getExtension().newExtensionDataBuilder();

        addValuesToBuilder(dataBuilder, extensionWrapper.getMethods().get(ExtensionMethod.ParameterType.PLAYER_STRING), parameters);
        addValuesToBuilder(dataBuilder, extensionWrapper.getMethods().get(ExtensionMethod.ParameterType.PLAYER_UUID), parameters);

        gatherPlayer(parameters, (ExtDataBuilder) dataBuilder);
    }

    public void updateValues() {
        Parameters parameters = Parameters.server(serverInfo.getServerUUID());
        ExtensionDataBuilder dataBuilder = extensionWrapper.getExtension().newExtensionDataBuilder();

        addValuesToBuilder(dataBuilder, extensionWrapper.getMethods().get(ExtensionMethod.ParameterType.SERVER_NONE), parameters);

        gather(parameters, (ExtDataBuilder) dataBuilder);
    }


    private void gatherPlayer(Parameters parameters, ExtDataBuilder dataBuilder) {
        Conditions conditions = new Conditions();
        for (ExtDataBuilder.ClassValuePair pair : dataBuilder.getValues()) {
            pair.getValue(Boolean.class).flatMap(data -> data.getMetadata(BooleanDataValue.class))
                    .ifPresent(data -> storePlayerBoolean(parameters, conditions, data));
            pair.getValue(Long.class).flatMap(data -> data.getMetadata(NumberDataValue.class))
                    .ifPresent(data -> storePlayerNumber(parameters, conditions, data));
            pair.getValue(Double.class).flatMap(data -> data.getMetadata(DoubleDataValue.class))
                    .ifPresent(data -> storePlayerDouble(parameters, conditions, data));
            pair.getValue(String.class).flatMap(data -> data.getMetadata(StringDataValue.class))
                    .ifPresent(data -> storePlayerString(parameters, conditions, data));
            pair.getValue(String[].class).flatMap(data -> data.getMetadata(GroupsDataValue.class))
                    .ifPresent(data -> storePlayerGroups(parameters, conditions, data));
            pair.getValue(Table.class).flatMap(data -> data.getMetadata(TableDataValue.class))
                    .ifPresent(data -> storePlayerTable(parameters, conditions, data));
        }
    }

    private void gather(Parameters parameters, ExtDataBuilder dataBuilder) {
        Conditions conditions = new Conditions();
        for (ExtDataBuilder.ClassValuePair pair : dataBuilder.getValues()) {
            pair.getValue(Boolean.class).flatMap(data -> data.getMetadata(BooleanDataValue.class))
                    .ifPresent(data -> storeBoolean(parameters, conditions, data));
            pair.getValue(Long.class).flatMap(data -> data.getMetadata(NumberDataValue.class))
                    .ifPresent(data -> storeNumber(parameters, conditions, data));
            pair.getValue(Double.class).flatMap(data -> data.getMetadata(DoubleDataValue.class))
                    .ifPresent(data -> storeDouble(parameters, conditions, data));
            pair.getValue(String.class).flatMap(data -> data.getMetadata(StringDataValue.class))
                    .ifPresent(data -> storeString(parameters, conditions, data));
            pair.getValue(Table.class).flatMap(data -> data.getMetadata(TableDataValue.class))
                    .ifPresent(data -> storeTable(parameters, conditions, data));
        }
    }

    private void storeBoolean(Parameters parameters, Conditions conditions, BooleanDataValue data) {
        ProviderInformation information = data.getInformation();
        Boolean value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }
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
        Long value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StoreServerNumberResultTransaction(information, parameters, value));
    }

    private void storeDouble(Parameters parameters, Conditions conditions, DoubleDataValue data) {
        ProviderInformation information = data.getInformation();
        Double value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StoreServerDoubleResultTransaction(information, parameters, value));
    }

    private void storeString(Parameters parameters, Conditions conditions, StringDataValue data) {
        ProviderInformation information = data.getInformation();
        String value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StoreServerStringResultTransaction(information, parameters, value));
    }

    private void storeTable(Parameters parameters, Conditions conditions, TableDataValue data) {
        ProviderInformation information = data.getInformation();
        Table value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        Database db = dbSystem.getDatabase();
        for (Icon icon : value.getIcons()) {
            if (icon != null) db.executeTransaction(new StoreIconTransaction(icon));
        }
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreTableProviderTransaction(information, parameters, value));
        db.executeTransaction(new StoreServerTableResultTransaction(information, parameters, value));
    }

    private void storePlayerBoolean(Parameters parameters, Conditions conditions, BooleanDataValue data) {
        ProviderInformation information = data.getInformation();
        Boolean value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }
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
        Long value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StorePlayerNumberResultTransaction(information, parameters, value));
    }

    private void storePlayerDouble(Parameters parameters, Conditions conditions, DoubleDataValue data) {
        ProviderInformation information = data.getInformation();
        Double value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StorePlayerDoubleResultTransaction(information, parameters, value));
    }

    private void storePlayerString(Parameters parameters, Conditions conditions, StringDataValue data) {
        ProviderInformation information = data.getInformation();
        String value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StorePlayerStringResultTransaction(information, parameters, value));
    }

    private void storePlayerGroups(Parameters parameters, Conditions conditions, GroupsDataValue data) {
        ProviderInformation information = data.getInformation();
        String[] value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        Database db = dbSystem.getDatabase();
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreProviderTransaction(information, parameters));
        db.executeTransaction(new StorePlayerGroupsResultTransaction(information, parameters, value));
    }

    private void storePlayerTable(Parameters parameters, Conditions conditions, TableDataValue data) {
        ProviderInformation information = data.getInformation();
        Table value = data.getValue();
        if (value == null) return;
        Optional<String> condition = information.getCondition();
        if (condition.isPresent() && conditions.isNotFulfilled(condition.get())) {
            return;
        }

        Database db = dbSystem.getDatabase();
        for (Icon icon : value.getIcons()) {
            if (icon != null) db.executeTransaction(new StoreIconTransaction(icon));
        }
        db.executeTransaction(new StoreIconTransaction(information.getIcon()));
        db.executeTransaction(new StoreTableProviderTransaction(information, parameters, value));
        db.executeTransaction(new StorePlayerTableResultTransaction(information, parameters, value));
    }
}