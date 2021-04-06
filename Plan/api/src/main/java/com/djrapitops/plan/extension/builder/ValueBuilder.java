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

import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.annotation.BooleanProvider;
import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.annotation.StringProvider;
import com.djrapitops.plan.extension.annotation.Tab;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;

import java.util.function.Supplier;

public interface ValueBuilder {

    ValueBuilder methodName(ExtensionMethod method);

    ValueBuilder description(String description);

    ValueBuilder priority(int priority);

    default ValueBuilder showInPlayerTable(boolean show) {
        if (show) return showInPlayerTable();
        return this;
    }

    ValueBuilder showInPlayerTable();

    default ValueBuilder icon(String iconName, Family iconFamily, Color iconColor) {
        return icon(Icon.called(iconName).of(iconFamily).of(iconColor).build());
    }

    ValueBuilder icon(Icon icon);

    ValueBuilder showOnTab(String tabName);

    default ValueBuilder showOnTab(Tab annotation) {
        if (annotation != null) return showOnTab(annotation.value());
        return this;
    }

    default ValueBuilder formatAsDateWithYear() {
        return format(FormatType.DATE_YEAR);
    }

    default ValueBuilder formatAsDateWithSeconds() {
        return format(FormatType.DATE_SECOND);
    }

    default ValueBuilder formatAsTimeAmount() {
        return format(FormatType.TIME_MILLISECONDS);
    }

    ValueBuilder format(FormatType formatType);

    ValueBuilder showAsPlayerPageLink();

    ValueBuilder hideFromUsers(BooleanProvider annotation);

    ValueBuilder conditional(Conditional conditional);

    default ValueBuilder showAsPlayerPageLink(StringProvider annotation) {
        if (annotation.playerName()) return showAsPlayerPageLink();
        return this;
    }

    DataValue<Boolean> buildBoolean(boolean value);

    DataValue<Boolean> buildBooleanProvidingCondition(boolean value, String providedCondition);

    DataValue<String> buildString(String value);

    DataValue<Long> buildNumber(long value);

    DataValue<Double> buildDouble(double value);

    DataValue<Double> buildPercentage(double percentage);

    DataValue<String[]> buildGroup(String[] groups);

    DataValue<Boolean> buildBoolean(Supplier<Boolean> value);

    DataValue<Boolean> buildBooleanProvidingCondition(Supplier<Boolean> value, String providedCondition);

    DataValue<String> buildString(Supplier<String> value);

    DataValue<Long> buildNumber(Supplier<Long> value);

    DataValue<Double> buildDouble(Supplier<Double> value);

    DataValue<Double> buildPercentage(Supplier<Double> percentage);

    DataValue<String[]> buildGroup(Supplier<String[]> groups);
}
