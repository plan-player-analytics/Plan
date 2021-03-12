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

import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.builder.DataValue;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.results.*;

public class ExtValueBuilder implements ValueBuilder {

    // TODO add Conditional stuff so that annotation implementation can use builders
    private final String text;
    private String description;
    private int priority = 0;
    private boolean showInPlayerTable = false;
    private Icon icon;
    private String tabName;

    private boolean formatAsPlayerName = false;
    private FormatType formatType = FormatType.NONE;

    public ExtValueBuilder(String text) {
        this.text = text;
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

    private ExtensionDescriptive getDescriptive() { // TODO use ProviderInformation instead
        return new ExtensionDescriptive(
                text.toLowerCase().replaceAll("\\s", ""),
                text,
                description,
                icon,
                priority
        );
    }

    @Override
    public DataValue<Boolean> buildBoolean(boolean value) {
        return new ExtensionBooleanData(getDescriptive(), value);
    }

    @Override
    public DataValue<String> buildString(String value) {
        return new ExtensionStringData(getDescriptive(), formatAsPlayerName, value);
    }

    @Override
    public DataValue<Long> buildNumber(long value) {
        return new ExtensionNumberData(getDescriptive(), formatType, value);
    }

    @Override
    public DataValue<Double> buildDouble(double value) {
        return new ExtensionDoubleData(getDescriptive(), value);
    }

    @Override
    public DataValue<Double> buildPercentage(double percentage) {
        return new ExtensionDoubleData(getDescriptive(), percentage);
    }

    @Override
    public DataValue<String[]> buildGroup(String[] groups) { // TODO
        return null;
    }
}
