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
package com.djrapitops.plan.extension.implementation;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.extractor.ExtensionExtractor;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.extractor.ExtensionMethods;
import com.djrapitops.plan.extension.extractor.MethodAnnotations;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.providers.*;
import com.djrapitops.plan.utilities.java.Lists;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents information defined in a {@link DataExtension} class.
 * <p>
 * Extracts objects that can be used to obtain data from a {@link DataExtension}.
 * <p>
 * Goal of this class is to abstract away DataExtension API annotations so that they will not be needed outside when calling methods.
 *
 * @author AuroraLS3
 */
public class ExtensionWrapper {

    private final ExtensionExtractor extractor;
    private final DataProviders providers;
    private final DataExtension extension;

    private final PluginInfo pluginInfo;
    private final List<TabInfo> tabInformation;
    private final Optional<TabOrder> tabOrder;
    private final Map<ExtensionMethod.ParameterType, ExtensionMethods> methods;

    /**
     * Create an ExtensionWrapper.
     *
     * @param extension DataExtension to extract information from.
     * @throws IllegalArgumentException If something is badly wrong with the specified extension class annotations.
     */
    public ExtensionWrapper(DataExtension extension) {
        this.extension = extension;
        extractor = new ExtensionExtractor(this.extension);

        pluginInfo = extractor.getPluginInfo();
        tabInformation = extractor.getTabInformation();
        tabOrder = extractor.getTabOrder();
        methods = extractor.getMethods();

        extractor.extractAnnotationInformation();
        providers = new DataProviders();
        extractProviders();
    }

    public CallEvents[] getCallEvents() {
        return extension.callExtensionMethodsOn();
    }

    public DataExtension getExtension() {
        return extension;
    }

    public String getPluginName() {
        return pluginInfo.name();
    }

    public Icon getPluginIcon() {
        return new Icon(pluginInfo.iconFamily(), pluginInfo.iconName(), pluginInfo.color());
    }

    public Collection<TabInformation> getPluginTabs() {
        Map<String, Integer> order = getTabOrder().map(this::orderToMap).orElse(new HashMap<>());

        Set<String> usedTabs = extractor.getTabAnnotations().stream()
                .map(Tab::value)
                .collect(Collectors.toSet());
        return extractor.getTabInformation()
                .stream()
                .filter(info -> usedTabs.contains(info.tab()))
                .map(tabInfo -> {
                    String tabName = tabInfo.tab();
                    return new TabInformation(
                            tabName,
                            new Icon(tabInfo.iconFamily(), tabInfo.iconName(), Color.NONE),
                            tabInfo.elementOrder(),
                            order.getOrDefault(tabName, 100)
                    );
                }).collect(Collectors.toList());
    }

    private Map<String, Integer> orderToMap(String[] order) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < order.length; i++) {
            map.put(order[i], i);
        }
        return map;
    }

    public Optional<String[]> getTabOrder() {
        return extractor.getTabOrder().map(TabOrder::value);
    }

    public Collection<String> getInvalidatedMethods() {
        return Lists.mapUnique(extractor.getInvalidateMethodAnnotations(), InvalidateMethod::value);
    }

    public DataProviders getProviders() {
        return providers;
    }

    @Deprecated
    private void extractProviders() {
        PluginInfo pluginInfo = extractor.getPluginInfo();

        MethodAnnotations methodAnnotations = extractor.getMethodAnnotations();
        Map<Method, Tab> tabs = methodAnnotations.getMethodAnnotations(Tab.class);
        Map<Method, Conditional> conditions = methodAnnotations.getMethodAnnotations(Conditional.class);

        extractProviders(pluginInfo, tabs, conditions, BooleanProvider.class, BooleanDataProvider::placeToDataProviders);
        extractProviders(pluginInfo, tabs, conditions, DoubleProvider.class, DoubleDataProvider::placeToDataProviders);
        extractProviders(pluginInfo, tabs, conditions, PercentageProvider.class, PercentageDataProvider::placeToDataProviders);
        extractProviders(pluginInfo, tabs, conditions, NumberProvider.class, NumberDataProvider::placeToDataProviders);
        extractProviders(pluginInfo, tabs, conditions, StringProvider.class, StringDataProvider::placeToDataProviders);
        extractProviders(pluginInfo, tabs, conditions, TableProvider.class, TableDataProvider::placeToDataProviders);
        extractProviders(pluginInfo, tabs, conditions, GroupProvider.class, GroupDataProvider::placeToDataProviders);
    }

    private <T extends Annotation> void extractProviders(PluginInfo pluginInfo, Map<Method, Tab> tabs, Map<Method, Conditional> conditions, Class<T> ofKind, DataProviderFactory<T> factory) {
        String pluginName = pluginInfo.name();

        for (Map.Entry<Method, T> entry : extractor.getMethodAnnotations().getMethodAnnotations(ofKind).entrySet()) {
            Method method = entry.getKey();
            T annotation = entry.getValue();
            Conditional conditional = conditions.get(method);
            Optional<Tab> tab = Optional.ofNullable(tabs.get(method));

            factory.placeToDataProviders(
                    providers, method, annotation,
                    conditional,
                    tab.map(Tab::value).orElse(null),
                    pluginName
            );
        }
    }

    public Collection<String> getWarnings() {
        return extractor.getWarnings();
    }

    /**
     * Functional interface for defining a method that places required DataProvider to DataProviders.
     *
     * @param <T> Type of the annotation on the method that is going to be extracted.
     */
    interface DataProviderFactory<T extends Annotation> {
        void placeToDataProviders(
                DataProviders dataProviders,
                Method method, T annotation, Conditional condition, String tab, String pluginName
        );
    }

    public ExtensionExtractor getExtractor() {
        return extractor;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public List<TabInfo> getTabInformation() {
        return tabInformation;
    }

    public Map<ExtensionMethod.ParameterType, ExtensionMethods> getMethods() {
        return methods;
    }
}