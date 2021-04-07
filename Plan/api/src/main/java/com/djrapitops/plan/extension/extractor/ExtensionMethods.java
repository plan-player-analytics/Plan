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
package com.djrapitops.plan.extension.extractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation detail, abstracts away method type reflection to a more usable API.
 */
public class ExtensionMethods {

    private final List<ExtensionMethod> booleanProviders;
    private final List<ExtensionMethod> numberProviders;
    private final List<ExtensionMethod> doubleProviders;
    private final List<ExtensionMethod> percentageProviders;
    private final List<ExtensionMethod> stringProviders;
    private final List<ExtensionMethod> tableProviders;
    private final List<ExtensionMethod> groupProviders;
    private final List<ExtensionMethod> dataBuilderProviders;

    public ExtensionMethods() {
        booleanProviders = new ArrayList<>();
        numberProviders = new ArrayList<>();
        doubleProviders = new ArrayList<>();
        percentageProviders = new ArrayList<>();
        stringProviders = new ArrayList<>();
        tableProviders = new ArrayList<>();
        groupProviders = new ArrayList<>();
        dataBuilderProviders = new ArrayList<>();
    }

    public List<ExtensionMethod> getBooleanProviders() {
        return booleanProviders;
    }

    public List<ExtensionMethod> getNumberProviders() {
        return numberProviders;
    }

    public List<ExtensionMethod> getDoubleProviders() {
        return doubleProviders;
    }

    public List<ExtensionMethod> getPercentageProviders() {
        return percentageProviders;
    }

    public List<ExtensionMethod> getStringProviders() {
        return stringProviders;
    }

    public List<ExtensionMethod> getTableProviders() {
        return tableProviders;
    }

    public List<ExtensionMethod> getGroupProviders() {
        return groupProviders;
    }

    public List<ExtensionMethod> getDataBuilderProviders() {
        return dataBuilderProviders;
    }

    public void addBooleanMethod(ExtensionMethod method) {
        booleanProviders.add(method);
    }

    public void addNumberMethod(ExtensionMethod method) {
        numberProviders.add(method);
    }

    public void addDoubleMethod(ExtensionMethod method) {
        doubleProviders.add(method);
    }

    public void addPercentageMethod(ExtensionMethod method) {
        percentageProviders.add(method);
    }

    public void addStringMethod(ExtensionMethod method) {
        stringProviders.add(method);
    }

    public void addTableMethod(ExtensionMethod method) {
        tableProviders.add(method);
    }

    public void addGroupMethod(ExtensionMethod method) {
        groupProviders.add(method);
    }

    public void addDataBuilderMethod(ExtensionMethod method) {
        dataBuilderProviders.add(method);
    }
}
