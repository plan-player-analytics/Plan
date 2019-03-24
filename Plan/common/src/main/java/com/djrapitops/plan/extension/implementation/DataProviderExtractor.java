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

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.extractor.ExtensionExtractor;
import com.djrapitops.plan.extension.extractor.MethodAnnotations;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.providers.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extracts objects that can be used to obtain data to store in the database.
 * <p>
 * Goal of this class is to abstract away DataExtension API annotations so that they will not be needed outside when calling methods.
 *
 * @author Rsl1122
 */
public class DataProviderExtractor {

    private ExtensionExtractor extensionExtractor;
    private DataProviders dataProviders;

    /**
     * Create a DataProviderExtractor.
     *
     * @param extension DataExtension to extract information from.
     * @throws IllegalArgumentException If something is badly wrong with the specified extension class annotations.
     */
    public DataProviderExtractor(DataExtension extension) {
        extensionExtractor = new ExtensionExtractor(extension);

        extensionExtractor.extractAnnotationInformation();

        dataProviders = new DataProviders();
        extractAllDataProviders();
    }

    public String getPluginName() {
        return extensionExtractor.getPluginInfo().name();
    }

    public Icon getPluginIcon() {
        PluginInfo pluginInfo = extensionExtractor.getPluginInfo();
        return new Icon(pluginInfo.iconFamily(), pluginInfo.iconName(), pluginInfo.color());
    }

    public Collection<TabInformation> getPluginTabs() {
        Map<String, TabInfo> tabInformation = extensionExtractor.getTabInformation()
                .stream().collect(Collectors.toMap(TabInfo::tab, Function.identity(), (one, two) -> one));

        Map<String, Integer> order = getTabOrder().map(this::orderToMap).orElse(new HashMap<>());

        // Extracts PluginTabs
        return extensionExtractor.getMethodAnnotations().getAnnotations(Tab.class).stream()
                .map(Tab::value)
                .distinct()
                .map(tabName -> {
                    Optional<TabInfo> tabInfo = Optional.ofNullable(tabInformation.get(tabName));
                    return new TabInformation(
                            tabName,
                            tabInfo.map(info -> new Icon(info.iconFamily(), info.iconName(), Color.NONE)).orElse(null),
                            tabInfo.map(TabInfo::elementOrder).orElse(null),
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
        return extensionExtractor.getTabOrder().map(TabOrder::value);
    }

    public Collection<String> getInvalidatedMethods() {
        return extensionExtractor.getInvalidateMethodAnnotations().stream()
                .map(InvalidateMethod::value)
                .collect(Collectors.toSet());
    }

    public DataProviders getDataProviders() {
        return dataProviders;
    }

    private void extractAllDataProviders() {
        PluginInfo pluginInfo = extensionExtractor.getPluginInfo();

        dataProviders.setCallPlayerMethodsOnPlayerLeave(pluginInfo.updatePlayerDataOnLeave());
        dataProviders.setCallServerMethodsPeriodically(pluginInfo.updateServerDataPeriodically());

        MethodAnnotations methodAnnotations = extensionExtractor.getMethodAnnotations();
        Map<Method, Tab> tabs = methodAnnotations.getMethodAnnotations(Tab.class);
        Map<Method, Conditional> conditions = methodAnnotations.getMethodAnnotations(Conditional.class);

        extractDataProviders(pluginInfo, tabs, conditions, BooleanProvider.class, BooleanDataProvider::placeToDataProviders);
        extractDataProviders(pluginInfo, tabs, conditions, DoubleProvider.class, DoubleDataProvider::placeToDataProviders);
        extractDataProviders(pluginInfo, tabs, conditions, PercentageProvider.class, PercentageDataProvider::placeToDataProviders);
        extractDataProviders(pluginInfo, tabs, conditions, NumberProvider.class, NumberDataProvider::placeToDataProviders);
        extractDataProviders(pluginInfo, tabs, conditions, StringProvider.class, StringDataProvider::placeToDataProviders);
    }

    private <T extends Annotation> void extractDataProviders(PluginInfo pluginInfo, Map<Method, Tab> tabs, Map<Method, Conditional> conditions, Class<T> ofKind, DataProviderFactory<T> factory) {
        for (Map.Entry<Method, T> entry : extensionExtractor.getMethodAnnotations().getMethodAnnotations(ofKind).entrySet()) {
            Method method = entry.getKey();
            T annotation = entry.getValue();
            Optional<Conditional> conditional = Optional.ofNullable(conditions.get(method));
            Optional<Tab> tab = Optional.ofNullable(tabs.get(method));

            factory.placeToDataProviders(
                    dataProviders, method, annotation,
                    conditional.map(Conditional::value).orElse(null),
                    tab.map(Tab::value).orElse(null),
                    pluginInfo.name()
            );
        }
    }

    public Collection<String> getWarnings() {
        return extensionExtractor.getWarnings();
    }

    /**
     * Functional interface for defining a method that places required DataProvider to DataProviders.
     *
     * @param <T> Type of the annotation on the method that is going to be extracted.
     */
    interface DataProviderFactory<T extends Annotation> {
        void placeToDataProviders(
                DataProviders dataProviders,
                Method method, T annotation, String condition, String tab, String pluginName
        );
    }
}