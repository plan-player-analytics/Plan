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
package com.djrapitops.plan.extension.implementation.builder;

import com.djrapitops.plan.component.Component;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.annotation.BooleanProvider;
import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.graph.DataPoint;
import com.djrapitops.plan.extension.graph.HistoryStrategy;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.table.Table;

import java.util.function.Supplier;

public class ExtValueBuilder implements ValueBuilder {

    private final String pluginName;
    private final String text;
    private String providerName;
    private String description;
    private int priority = 0;
    private boolean showInPlayerTable = false;
    private Icon icon;
    private String tabName;

    private boolean hidden = false;
    private boolean formatAsPlayerName = false;
    private FormatType formatType = FormatType.NONE;
    private Conditional conditional;

    public ExtValueBuilder(String text, DataExtension extension) {
        this.text = text;
        pluginName = extension.getClass().getAnnotation(PluginInfo.class).name();
    }

    public static String formatTextAsIdentifier(String text) {
        return text.toLowerCase().replaceAll("\\s", "");
    }

    @Override
    public ValueBuilder methodName(ExtensionMethod method) {
        this.providerName = method.getMethod().getName();
        return this;
    }

    @Override
    public ValueBuilder description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public ValueBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public ValueBuilder showInPlayerTable() {
        this.showInPlayerTable = true;
        return this;
    }

    @Override
    public ValueBuilder icon(Icon icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public ValueBuilder showOnTab(String tabName) {
        this.tabName = tabName;
        return this;
    }

    @Override
    public ValueBuilder format(FormatType formatType) {
        this.formatType = formatType;
        return this;
    }

    @Override
    public ValueBuilder showAsPlayerPageLink() {
        formatAsPlayerName = true;
        return this;
    }

    @Override
    public ValueBuilder hideFromUsers(BooleanProvider annotation) {
        this.hidden = annotation.hidden();
        return this;
    }

    @Override
    public ValueBuilder conditional(Conditional conditional) {
        this.conditional = conditional;
        return this;
    }

    private ProviderInformation getProviderInformation() {
        return getProviderInformation(false, false, null);
    }

    private ProviderInformation getBooleanProviderInformation(String providedCondition) {
        return getProviderInformation(false, false, providedCondition);
    }

    private ProviderInformation getComponentProviderInformation() {
        return getProviderInformation(false, true, null);
    }

    private ProviderInformation getPercentageProviderInformation() {
        return getProviderInformation(true, false, null);
    }

    private ProviderInformation getProviderInformation(boolean percentage, boolean component, String providedCondition) {
        ProviderInformation.Builder builder = ProviderInformation.builder(pluginName)
                .setName(providerName != null ? providerName
                        : formatTextAsIdentifier(text))
                .setText(text)
                .setDescription(description)
                .setPriority(priority)
                .setIcon(icon)
                .setShowInPlayersTable(showInPlayerTable)
                .setTab(tabName)
                .setPlayerName(formatAsPlayerName)
                .setFormatType(formatType)
                .setHidden(hidden)
                .setCondition(conditional);

        if (percentage) {
            builder = builder.setAsPercentage();
        }

        if (component) {
            builder = builder.setAsComponent();
        }

        if (providedCondition != null && !providedCondition.isEmpty()) {
            builder = builder.setProvidedCondition(providedCondition);
        }

        return builder.build();
    }

    private ProviderInformation getTableProviderInformation(Color tableColor) {
        return ProviderInformation.builder(pluginName)
                .setName(providerName != null ? providerName
                        : formatTextAsIdentifier(text))
                .setPriority(0)
                .setCondition(conditional)
                .setTab(tabName)
                .setTableColor(tableColor)
                .build();
    }

    private ProviderInformation getGraphHistoryProviderInformation(String methodName, HistoryStrategy appendStrategy) {
        return ProviderInformation.builder(pluginName)
                .setName(methodName)
                .setPriority(0)
                .setTab(tabName)
                .setAppendStrategy(appendStrategy)
                .build();
    }

    @Override
    public DataValue<Boolean> buildBoolean(boolean value) {
        return new BooleanDataValue(value, getProviderInformation());
    }

    @Override
    public DataValue<Boolean> buildBooleanProvidingCondition(boolean value, String providedCondition) {
        return new BooleanDataValue(value, getBooleanProviderInformation(providedCondition));
    }

    @Override
    public DataValue<String> buildString(String value) {
        return new StringDataValue(value, getProviderInformation());
    }

    @Override
    public DataValue<Component> buildComponent(Component value) {
        return new ComponentDataValue(value, getComponentProviderInformation());
    }

    @Override
    public DataValue<Long> buildNumber(Long value) {
        return new NumberDataValue(value, getProviderInformation());
    }

    @Override
    public DataValue<Double> buildDouble(Double value) {
        return new DoubleDataValue(value, getProviderInformation());
    }

    @Override
    public DataValue<Double> buildPercentage(Double value) {
        return new DoubleDataValue(value, getPercentageProviderInformation());
    }

    @Override
    public DataValue<String[]> buildGroup(String[] groups) {
        return new GroupsDataValue(groups, getProviderInformation());
    }

    @Override
    public DataValue<Boolean> buildBoolean(Supplier<Boolean> value) {
        return new BooleanDataValue(value, getProviderInformation());
    }

    @Override
    public DataValue<Boolean> buildBooleanProvidingCondition(Supplier<Boolean> value, String providedCondition) {
        return new BooleanDataValue(value, getBooleanProviderInformation(providedCondition));
    }

    @Override
    public DataValue<String> buildString(Supplier<String> value) {
        return new StringDataValue(value, getProviderInformation());
    }

    @Override
    public DataValue<Component> buildComponent(Supplier<Component> value) {
        return new ComponentDataValue(value, getComponentProviderInformation());
    }

    @Override
    public DataValue<Long> buildNumber(Supplier<Long> value) {
        return new NumberDataValue(value, getProviderInformation());
    }

    @Override
    public DataValue<Double> buildDouble(Supplier<Double> value) {
        return new DoubleDataValue(value, getProviderInformation());
    }

    @Override
    public DataValue<Double> buildPercentage(Supplier<Double> percentage) {
        return new DoubleDataValue(percentage, getPercentageProviderInformation());
    }

    @Override
    public DataValue<String[]> buildGroup(Supplier<String[]> groups) {
        return new GroupsDataValue(groups, getProviderInformation());
    }

    @Override
    public DataValue<DataPoint[]> buildGraphHistoryPoints(Supplier<DataPoint[]> historyData, String methodName, HistoryStrategy appendStrategy) {
        return new GraphHistoryPoints(historyData, getGraphHistoryProviderInformation(methodName, appendStrategy));
    }

    @Override
    public DataValue<Table> buildTable(Table table, Color tableColor) {
        return new TableDataValue(table, getTableProviderInformation(tableColor));
    }

    @Override
    public DataValue<Table> buildTable(Supplier<Table> table, Color tableColor) {
        return new TableDataValue(table, getTableProviderInformation(tableColor));
    }
}
