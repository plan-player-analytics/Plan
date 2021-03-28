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
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;

public interface ValueBuilder {

    ValueBuilder description(String description);

    ValueBuilder priority(int priority);

    ValueBuilder showInPlayerTable();

    default ValueBuilder icon(String iconName, Family iconFamily, Color iconColor) {
        return icon(Icon.called(iconName).of(iconFamily).of(iconColor).build());
    }

    ValueBuilder icon(Icon icon);

    ValueBuilder showOnTab(String tabName);

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

    DataValue<Boolean> buildBoolean(boolean value);

    DataValue<Boolean> buildBooleanProvidingCondition(boolean value, String providedCondition);

    DataValue<String> buildString(String value);

    DataValue<Long> buildNumber(long value);

    DataValue<Double> buildDouble(double value);

    DataValue<Double> buildPercentage(double percentage);

    DataValue<String[]> buildGroup(String[] groups);

}
