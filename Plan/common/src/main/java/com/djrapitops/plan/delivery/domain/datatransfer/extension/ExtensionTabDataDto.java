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
package com.djrapitops.plan.delivery.domain.datatransfer.extension;

import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ExtensionTabDataDto(
        @Nullable(/* no tab was defined for provider */) TabInformationDto tabInformation,
        List<ExtensionValueDataDto> values,
        List<ExtensionTableDataDto> tableData,
        List<ExtensionStatisticsDataDto> statisticsData,
        List<String> graphNames
) {

    public ExtensionTabDataDto(ExtensionTabData extensionTabData) {
        this(
                new TabInformationDto(extensionTabData.getTabInformation()),
                constructValues(extensionTabData.getValueOrder(), extensionTabData),
                extensionTabData.getTableData().stream().map(ExtensionTableDataDto::new).toList(),
                extensionTabData.getStatisticsData().stream().map(ExtensionStatisticsDataDto::new).toList(),
                extensionTabData.getGraphNames()
        );
    }

    public static Optional<ExtensionValueDataDto> mapToValue(ExtensionTabData tabData, String key) {
        Formatters formatters = Formatters.getInstance();

        Optional<ExtensionValueDataDto> booleanValue = tabData.getBoolean(key).map(data -> new ExtensionValueDataDto(data.getDescription(), "BOOLEAN", data.getValue()));
        if (booleanValue.isPresent()) return booleanValue;
        Optional<ExtensionValueDataDto> doubleValue = tabData.getDouble(key).map(data -> new ExtensionValueDataDto(data.getDescription(), "DOUBLE", data.getFormattedValue(formatters.decimals())));
        if (doubleValue.isPresent()) return doubleValue;
        Optional<ExtensionValueDataDto> percentage = tabData.getPercentage(key).map(data -> new ExtensionValueDataDto(data.getDescription(), "PERCENTAGE", data.getFormattedValue(formatters.percentage())));
        if (percentage.isPresent()) return percentage;
        Optional<ExtensionValueDataDto> number = tabData.getNumber(key).map(data -> new ExtensionValueDataDto(data.getDescription(), data.getFormatType() == FormatType.NONE ? "NUMBER" : data.getFormatType().name(), data.getRawValue()));
        if (number.isPresent()) return number;
        Optional<ExtensionValueDataDto> string = tabData.getString(key).map(data -> new ExtensionValueDataDto(data.getDescription(), data.isPlayerName() ? "LINK" : "STRING", data.getFormattedValue()));
        if (string.isPresent()) return string;

        // If component is not found either return empty Optional.
        return tabData.getComponent(key).map(data -> new ExtensionValueDataDto(data.getDescription(), "COMPONENT", data.getFormattedValue()));
    }

    private static List<ExtensionValueDataDto> constructValues(List<String> order, ExtensionTabData tabData) {
        List<ExtensionValueDataDto> extensionValues = new ArrayList<>();
        for (String key : order) {
            mapToValue(tabData, key).ifPresent(extensionValues::add);
        }
        return extensionValues;
    }
}
