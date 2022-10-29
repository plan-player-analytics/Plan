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
import com.djrapitops.plan.utilities.java.Lists;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents data on an extension tab.
 *
 * @author AuroraLS3
 */
public class ExtensionTabData implements Comparable<ExtensionTabData> {

    private final TabInformation tabInformation; // Can be null in case where no tab was defined for provider.

    private final Map<String, ExtensionBooleanData> booleanData;
    private final Map<String, ExtensionDoubleData> doubleData;
    private final Map<String, ExtensionDoubleData> percentageData;
    private final Map<String, ExtensionNumberData> numberData;
    private final Map<String, ExtensionStringData> stringData;
    private final Map<String, ExtensionComponentData> componentData;

    private final List<ExtensionTableData> tableData;
    private final List<ExtensionDescription> descriptions;

    private List<String> order;

    // Table and Graph data will be added later.

    public ExtensionTabData(TabInformation tabInformation) {
        this.tabInformation = tabInformation;

        booleanData = new HashMap<>();
        doubleData = new HashMap<>();
        percentageData = new HashMap<>();
        numberData = new HashMap<>();
        stringData = new HashMap<>();
        componentData = new HashMap<>();

        tableData = new ArrayList<>();
        descriptions = new ArrayList<>();
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

    public Optional<ExtensionComponentData> getComponent(String providerName) {
        return Optional.ofNullable(componentData.get(providerName));
    }

    public List<ExtensionTableData> getTableData() {
        return tableData;
    }

    /**
     * Get all descriptions for data in this tab.
     * <p>
     * Only available after the Tab has been built.
     *
     * @return List of {@link ExtensionDescription}s.
     */
    public List<ExtensionDescription> getDescriptions() {
        return descriptions;
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
        this.componentData.putAll(other.componentData);

        this.tableData.addAll(other.tableData);

        createOrderingList();
    }

    private void createOrderingList() {
        descriptions.addAll(Lists.map(booleanData.values(), DescribedExtensionData::getDescription));
        descriptions.addAll(Lists.map(doubleData.values(), DescribedExtensionData::getDescription));
        descriptions.addAll(Lists.map(percentageData.values(), DescribedExtensionData::getDescription));
        descriptions.addAll(Lists.map(numberData.values(), DescribedExtensionData::getDescription));
        descriptions.addAll(Lists.map(stringData.values(), DescribedExtensionData::getDescription));
        descriptions.addAll(Lists.map(componentData.values(), DescribedExtensionData::getDescription));

        order = descriptions.stream().sorted()
                .map(ExtensionDescription::getName)
                .distinct()// Method names are usually different, but in case someone had same method name with different parameters.
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ExtensionTabData{" +
                "tabInformation=" + tabInformation +
                ", tableData=" + tableData +
                ", descriptions=" + descriptions +
                '}';
    }

    public static class Builder {

        private final ExtensionTabData data;

        public Builder(TabInformation tabInformation) {
            data = new ExtensionTabData(tabInformation);
        }

        public Builder putBooleanData(ExtensionBooleanData extensionBooleanData) {
            data.booleanData.put(extensionBooleanData.getDescription().getName(), extensionBooleanData);
            return this;
        }

        public Builder putDoubleData(ExtensionDoubleData extensionDoubleData) {
            data.doubleData.put(extensionDoubleData.getDescription().getName(), extensionDoubleData);
            return this;
        }

        public Builder putPercentageData(ExtensionDoubleData extensionDoubleData) {
            data.percentageData.put(extensionDoubleData.getDescription().getName(), extensionDoubleData);
            return this;
        }

        public Builder putNumberData(ExtensionNumberData extensionNumberData) {
            data.numberData.put(extensionNumberData.getDescription().getName(), extensionNumberData);
            return this;
        }

        public Builder putStringData(ExtensionStringData extensionStringData) {
            data.stringData.put(extensionStringData.getDescription().getName(), extensionStringData);
            return this;
        }

        public Builder putComponentData(ExtensionComponentData extensionComponentData) {
            data.componentData.put(extensionComponentData.getDescription().getName(), extensionComponentData);
            return this;
        }

        public Builder putGroupData(ExtensionStringData extensionStringData) {
            String name = extensionStringData.getDescription().getName();
            ExtensionStringData previous = data.stringData.get(name);
            data.stringData.put(name, previous != null ? previous.concatenate(extensionStringData) : extensionStringData);
            return this;
        }

        public Builder putTableData(ExtensionTableData extensionTableData) {
            data.tableData.add(extensionTableData);
            return this;
        }

        public ExtensionTabData build() {
            data.createOrderingList();
            Collections.sort(data.tableData);
            return data;
        }
    }
}