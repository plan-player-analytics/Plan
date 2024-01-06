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
package com.djrapitops.plan.extension.builder;

import com.djrapitops.plan.component.Component;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.annotation.BooleanProvider;
import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.annotation.StringProvider;
import com.djrapitops.plan.extension.annotation.Tab;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;

import java.util.function.Supplier;

/**
 * Used for building {@link DataValue}s for {@link ExtensionDataBuilder#addValue(Class, DataValue)}.
 * <p>
 * Requires Capability DATA_EXTENSION_BUILDER_API
 * <p>
 * Obtain an instance with {@link ExtensionDataBuilder#valueBuilder(String)}.
 */
public interface ValueBuilder {

    /**
     * Description about the value that is shown on hover.
     *
     * @param description Describe what the value is about, maximum 150 characters.
     * @return This builder.
     */
    ValueBuilder description(String description);

    /**
     * Display-priority of the value, highest value is placed top most.
     * <p>
     * Two values with same priority may appear in a random order.
     *
     * @param priority Priority between 0 and {@code Integer.MAX_VALUE}.
     * @return This builder.
     */
    ValueBuilder priority(int priority);

    /**
     * Show this value in the players table.
     *
     * @return This builder.
     */
    ValueBuilder showInPlayerTable();

    /**
     * Icon displayed next to the value.
     * <p>
     * See <a href="https://fontawesome.com/icons">FontAwesome</a> (select 'free')) for icons
     *
     * @param iconName   Name of the icon
     * @param iconFamily Family of the icon
     * @param iconColor  Color of the icon
     * @return This builder.
     */
    default ValueBuilder icon(String iconName, Family iconFamily, Color iconColor) {
        return icon(Icon.called(iconName).of(iconFamily).of(iconColor).build());
    }

    /**
     * Icon displayed next to the value.
     * <p>
     * See <a href="https://fontawesome.com/icons">FontAwesome</a> (select 'free')) for icons
     *
     * @param icon Icon built using the methods in {@link Icon}.
     * @return This builder.
     */
    ValueBuilder icon(Icon icon);

    /**
     * Show the value on a specific tab.
     * <p>
     * Remember to define {@link com.djrapitops.plan.extension.annotation.TabInfo} annotation.
     *
     * @param tabName Name of the tab.
     * @return This builder.
     */
    ValueBuilder showOnTab(String tabName);

    /**
     * {@link ValueBuilder#buildNumber(Long)} specific method, format the value as a epoch ms timestamp.
     *
     * @return This builder.
     */
    default ValueBuilder formatAsDateWithYear() {
        return format(FormatType.DATE_YEAR);
    }

    /**
     * {@link ValueBuilder#buildNumber(Long)} specific method, format the value as a epoch ms timestamp.
     *
     * @return This builder.
     */
    default ValueBuilder formatAsDateWithSeconds() {
        return format(FormatType.DATE_SECOND);
    }

    /**
     * {@link ValueBuilder#buildNumber(Long)} specific method, format the value as milliseconds of time.
     *
     * @return This builder.
     */
    default ValueBuilder formatAsTimeAmount() {
        return format(FormatType.TIME_MILLISECONDS);
    }

    /**
     * {@link ValueBuilder#buildNumber(Long)} specific method, format the value with {@link FormatType}
     *
     * @return This builder.
     */
    ValueBuilder format(FormatType formatType);

    /**
     * {@link ValueBuilder#buildString(String)} specific method, link the value to a player page.
     *
     * @return This builder.
     */
    ValueBuilder showAsPlayerPageLink();

    /**
     * Build a Boolean. Displayed as "Yes/No" on the page.
     *
     * @param value true/false
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    DataValue<Boolean> buildBoolean(boolean value);

    /**
     * Build a Boolean that provides a value for {@link Conditional}. Displayed as "Yes/No" on the page.
     *
     * @param value true/false
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    DataValue<Boolean> buildBooleanProvidingCondition(boolean value, String providedCondition);

    /**
     * Build a String.
     *
     * @param value any string. Limited to 50 characters.
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    DataValue<String> buildString(String value);

    /**
     * Build a {@link Component}.
     *
     * @param value a {@link Component} made by {@link com.djrapitops.plan.component.ComponentService}
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    DataValue<Component> buildComponent(Component value);

    /**
     * Build a Number.
     *
     * @param value a non-floating point number.
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    DataValue<Long> buildNumber(Long value);

    /**
     * Build a Number.
     *
     * @param value a non-floating point number.
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    default DataValue<Long> buildNumber(Integer value) {
        return buildNumber(value != null ? (long) value : null);
    }

    /**
     * Build a Number.
     *
     * @param value a non-floating point number.
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    default DataValue<Long> buildNumber(Double value) {
        return buildNumber(value != null ? (long) (double) value : null);
    }

    /**
     * Build a Floating point number.
     *
     * @param value a floating point number.
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    DataValue<Double> buildDouble(Double value);

    /**
     * Build a Percentage.
     *
     * @param percentage value between 0.0 and 1.0
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    DataValue<Double> buildPercentage(Double percentage);

    /**
     * Build a list of groups.
     *
     * @param groups names of groups a player is in.
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    DataValue<String[]> buildGroup(String[] groups);

    /**
     * Build a table.
     *
     * @param table      Table built using {@link Table#builder()}
     * @param tableColor Color of the table
     * @return a data value to give to {@link ExtensionDataBuilder}.
     */
    DataValue<Table> buildTable(Table table, Color tableColor);

    /**
     * Lambda version for conditional return or throwing {@link com.djrapitops.plan.extension.NotReadyException}.
     * <p>
     * {@link ValueBuilder#buildBoolean(boolean)}
     */
    DataValue<Boolean> buildBoolean(Supplier<Boolean> value);

    /**
     * Lambda version for conditional return or throwing {@link com.djrapitops.plan.extension.NotReadyException}.
     * <p>
     * {@link ValueBuilder#buildBooleanProvidingCondition(boolean, String)}
     */
    DataValue<Boolean> buildBooleanProvidingCondition(Supplier<Boolean> value, String providedCondition);

    /**
     * Lambda version for conditional return or throwing {@link com.djrapitops.plan.extension.NotReadyException}.
     * <p>
     * {@link ValueBuilder#buildString(String)}
     */
    DataValue<String> buildString(Supplier<String> value);

    /**
     * Lambda version for conditional return or throwing {@link com.djrapitops.plan.extension.NotReadyException}.
     * <p>
     * {@link ValueBuilder#buildComponent(Component)}
     */
    DataValue<Component> buildComponent(Supplier<Component> value);

    /**
     * Lambda version for conditional return or throwing {@link com.djrapitops.plan.extension.NotReadyException}.
     * <p>
     * {@link ValueBuilder#buildNumber(Long)}
     */
    DataValue<Long> buildNumber(Supplier<Long> value);

    /**
     * Lambda version for conditional return or throwing {@link com.djrapitops.plan.extension.NotReadyException}.
     * <p>
     * {@link ValueBuilder#buildDouble(Double)}
     */
    DataValue<Double> buildDouble(Supplier<Double> value);

    /**
     * Lambda version for conditional return or throwing {@link com.djrapitops.plan.extension.NotReadyException}.
     * <p>
     * {@link ValueBuilder#buildPercentage(Double)}
     */
    DataValue<Double> buildPercentage(Supplier<Double> percentage);

    /**
     * Lambda version for conditional return or throwing {@link com.djrapitops.plan.extension.NotReadyException}.
     * <p>
     * {@link ValueBuilder#buildGroup(String[])}
     */
    DataValue<String[]> buildGroup(Supplier<String[]> groups);

    /**
     * Lambda version for conditional return or throwing {@link com.djrapitops.plan.extension.NotReadyException}.
     * <p>
     * {@link ValueBuilder#buildTable(Table, Color)}
     */
    DataValue<Table> buildTable(Supplier<Table> table, Color tableColor);

    /**
     * Implementation detail - for abstracting annotations with the builder API.
     *
     * @param annotation BooleanProvider annotation.
     * @return This builder.
     */
    ValueBuilder hideFromUsers(BooleanProvider annotation);

    /**
     * Implementation detail - for abstracting annotations with the builder API.
     *
     * @param conditional Conditional annotation.
     * @return This builder.
     */
    ValueBuilder conditional(Conditional conditional);

    /**
     * Implementation detail - for abstracting annotations with the builder API.
     *
     * @param annotation StringProvider annotation.
     * @return This builder.
     */
    default ValueBuilder showAsPlayerPageLink(StringProvider annotation) {
        if (annotation.playerName()) return showAsPlayerPageLink();
        return this;
    }

    /**
     * Implementation detail - for abstracting annotations with the builder API.
     *
     * @param method Method this value is from.
     * @return This builder.
     */
    ValueBuilder methodName(ExtensionMethod method);

    /**
     * Implementation detail - for abstracting annotations with the builder API.
     *
     * @param show true/false
     * @return This builder.
     */
    default ValueBuilder showInPlayerTable(boolean show) {
        if (show) return showInPlayerTable();
        return this;
    }

    /**
     * Implementation detail - for abstracting annotations with the builder API.
     *
     * @param annotation Tab annotation.
     * @return This builder.
     */
    default ValueBuilder showOnTab(Tab annotation) {
        if (annotation != null) return showOnTab(annotation.value());
        return this;
    }
}
