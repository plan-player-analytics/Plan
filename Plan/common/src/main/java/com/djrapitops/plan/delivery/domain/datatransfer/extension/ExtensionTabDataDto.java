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

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;

import java.util.*;
import java.util.stream.Collectors;

public class ExtensionTabDataDto {

    private final TabInformationDto tabInformation; // Can be null in case where no tab was defined for provider.
    private final List<ExtensionValueDataDto> values;
    private final List<ExtensionTableDataDto> tableData;

    public ExtensionTabDataDto(ExtensionTabData extensionTabData) {
        this.tabInformation = new TabInformationDto(extensionTabData.getTabInformation());
        values = constructValues(extensionTabData.getValueOrder(), extensionTabData);
        tableData = extensionTabData.getTableData().stream().map(ExtensionTableDataDto::new).collect(Collectors.toList());
    }

    private List<ExtensionValueDataDto> constructValues(List<String> order, ExtensionTabData tabData) {
        Formatters formatters = Formatters.getInstance();
        Formatter<Double> decimalFormatter = formatters.decimals();
        Formatter<Double> percentageFormatter = formatters.percentage();

        Map<FormatType, Formatter<Long>> numberFormatters = new EnumMap<>(FormatType.class);
        numberFormatters.put(FormatType.DATE_SECOND, formatters.secondLong());
        numberFormatters.put(FormatType.DATE_YEAR, formatters.yearLong());
        numberFormatters.put(FormatType.TIME_MILLISECONDS, formatters.timeAmount());
        numberFormatters.put(FormatType.NONE, Object::toString);

        List<ExtensionValueDataDto> extensionValues = new ArrayList<>();
        for (String key : order) {
            tabData.getBoolean(key).ifPresent(data -> extensionValues.add(new ExtensionValueDataDto(data.getDescription(), "BOOLEAN", data.getFormattedValue())));
            tabData.getDouble(key).ifPresent(data -> extensionValues.add(new ExtensionValueDataDto(data.getDescription(), "DOUBLE", data.getFormattedValue(decimalFormatter))));
            tabData.getPercentage(key).ifPresent(data -> extensionValues.add(new ExtensionValueDataDto(data.getDescription(), "PERCENTAGE", data.getFormattedValue(percentageFormatter))));
            tabData.getNumber(key).ifPresent(data -> extensionValues.add(new ExtensionValueDataDto(data.getDescription(), data.getFormatType() == FormatType.NONE ? "NUMBER" : data.getFormatType().name(), data.getFormattedValue(numberFormatters.get(data.getFormatType())))));
            tabData.getString(key).ifPresent(data -> extensionValues.add(new ExtensionValueDataDto(data.getDescription(), data.isPlayerName() ? "HTML" : "STRING", data.getFormattedValue())));
        }
        return extensionValues;
    }

    public TabInformationDto getTabInformation() {
        return tabInformation;
    }

    public List<ExtensionValueDataDto> getValues() {
        return values;
    }

    public List<ExtensionTableDataDto> getTableData() {
        return tableData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionTabDataDto that = (ExtensionTabDataDto) o;
        return Objects.equals(getTabInformation(), that.getTabInformation()) && Objects.equals(getValues(), that.getValues()) && Objects.equals(getTableData(), that.getTableData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTabInformation(), getValues(), getTableData());
    }

    @Override
    public String toString() {
        return "ExtensionTabDataDto{" +
                "tabInformation=" + tabInformation +
                ", values=" + values +
                ", tableData=" + tableData +
                '}';
    }
}
