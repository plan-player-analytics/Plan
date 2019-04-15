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
package com.djrapitops.plan.extension.implementation.results;

import com.djrapitops.plan.extension.implementation.TabInformation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents data on an extension tab.
 *
 * @author Rsl1122
 */
public class ExtensionTabData implements Comparable<ExtensionTabData> {

    private final TabInformation tabInformation; // Can be null in case where no tab was defined for provider.

    private final Map<String, ExtensionBooleanData> booleanData;
    private final Map<String, ExtensionDoubleData> doubleData;
    private final Map<String, ExtensionDoubleData> percentageData;
    private final Map<String, ExtensionNumberData> numberData;
    private final Map<String, ExtensionStringData> stringData;

    private final List<ExtensionTableData> tableData;

    private List<String> order;

    // Table and Graph data will be added later.

    public ExtensionTabData(TabInformation tabInformation) {
        this.tabInformation = tabInformation;

        booleanData = new HashMap<>();
        doubleData = new HashMap<>();
        percentageData = new HashMap<>();
        numberData = new HashMap<>();
        stringData = new HashMap<>();

        tableData = new ArrayList<>();
    }

    public TabInformation getTabInformation() {
        return tabInformation;
    }

    public List<String> getValueOrder() {
        return order;
    }

    public Optional<ExtensionBooleanData> getBoolean(String providerName) {
        return Optional.ofNullable(booleanData.get(providerName));
    }

    public Optional<ExtensionDoubleData> getDouble(String providerName) {
        return Optional.ofNullable(doubleData.get(providerName));
    }

    public Optional<ExtensionDoubleData> getPercentage(String providerName) {
        return Optional.ofNullable(percentageData.get(providerName));
    }

    public Optional<ExtensionNumberData> getNumber(String providerName) {
        return Optional.ofNullable(numberData.get(providerName));
    }

    public Optional<ExtensionStringData> getString(String providerName) {
        return Optional.ofNullable(stringData.get(providerName));
    }

    public List<ExtensionTableData> getTableData() {
        return tableData;
    }

    @Override
    public int compareTo(ExtensionTabData other) {
        return Integer.compare(this.tabInformation.getTabPriority(), other.tabInformation.getTabPriority()); // Lower is first
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionTabData)) return false;
        ExtensionTabData that = (ExtensionTabData) o;
        return tabInformation.equals(that.tabInformation) &&
                order.equals(that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tabInformation, order);
    }

    public void combine(ExtensionTabData other) {
        this.booleanData.putAll(other.booleanData);
        this.doubleData.putAll(other.doubleData);
        this.percentageData.putAll(other.percentageData);
        this.numberData.putAll(other.numberData);
        this.stringData.putAll(other.stringData);

        this.tableData.addAll(other.tableData);

        createOrderingList();
    }

    private void createOrderingList() {
        List<ExtensionDescriptive> descriptives = new ArrayList<>();
        booleanData.values().stream().map(ExtensionData::getDescriptive).forEach(descriptives::add);
        doubleData.values().stream().map(ExtensionData::getDescriptive).forEach(descriptives::add);
        percentageData.values().stream().map(ExtensionData::getDescriptive).forEach(descriptives::add);
        numberData.values().stream().map(ExtensionData::getDescriptive).forEach(descriptives::add);
        stringData.values().stream().map(ExtensionData::getDescriptive).forEach(descriptives::add);

        order = descriptives.stream().sorted()
                .map(ExtensionDescriptive::getName)
                .distinct()// Method names are usually different, but in case someone had same method name with different parameters.
                .collect(Collectors.toList());
    }

    public static class Factory {

        private final ExtensionTabData data;

        public Factory(TabInformation tabInformation) {
            data = new ExtensionTabData(tabInformation);
        }

        public Factory putBooleanData(ExtensionBooleanData extensionBooleanData) {
            data.booleanData.put(extensionBooleanData.getDescriptive().getName(), extensionBooleanData);
            return this;
        }

        public Factory putDoubleData(ExtensionDoubleData extensionDoubleData) {
            data.doubleData.put(extensionDoubleData.getDescriptive().getName(), extensionDoubleData);
            return this;
        }

        public Factory putPercentageData(ExtensionDoubleData extensionDoubleData) {
            data.percentageData.put(extensionDoubleData.getDescriptive().getName(), extensionDoubleData);
            return this;
        }

        public Factory putNumberData(ExtensionNumberData extensionNumberData) {
            data.numberData.put(extensionNumberData.getDescriptive().getName(), extensionNumberData);
            return this;
        }

        public Factory putStringData(ExtensionStringData extensionStringData) {
            data.stringData.put(extensionStringData.getDescriptive().getName(), extensionStringData);
            return this;
        }

        public Factory putTableData(ExtensionTableData extensionTableData) {
            data.tableData.add(extensionTableData);
            return this;
        }

        public ExtensionTabData build() {
            data.createOrderingList();
            Collections.sort(data.tableData);
            return data;
        }
    }

    @Override
    public String toString() {
        return "ExtensionTabData{" +
                "available=" + order +
                '}';
    }
}