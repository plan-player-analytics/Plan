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

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation details for extracting methods from {@link com.djrapitops.plan.extension.DataExtension}.
 * <p>
 * This class can be used for testing validity of annotation implementations
 * in your unit tests to avoid runtime errors. {@link ExtensionExtractor#validateAnnotations()}
 *
 * @author Rsl1122
 */
public final class ExtensionExtractor {

    private final DataExtension extension;

    private final List<String> errors = new ArrayList<>();

    public ExtensionExtractor(DataExtension extension) {
        this.extension = extension;
    }

    /**
     * Use this method in an unit test to validate your DataExtension.
     *
     * @throws IllegalArgumentException If an implementation error is found.
     */
    public void validateAnnotations() {
        extractPluginInfo();
        extractTabInfo();
        List<Method> booleanMethods = extractBooleanMethods();
        extractConditionals(booleanMethods);
        extractNumberMethods();
        extractDoubleMethods();
        extractPercentageMethods();
        extractStringMethods();
        if (errors.isEmpty()) {
            return;
        }
        throw new IllegalArgumentException("Found errors: " + errors.toString());
    }

    public PluginInfo extractPluginInfo() {
        Class<? extends DataExtension> extClass = extension.getClass();
        PluginInfo info = extClass.getAnnotation(PluginInfo.class);

        if (info.name().length() > 50) {
            errors.add(extClass.getName() + "Plugin name was over 50 characters.");
        }

        return info;
    }

    public List<TabInfo> extractTabInfo() {
        Set<String> tabNames = new HashSet<>();
        List<TabInfo> tabInformation = new ArrayList<>();

        Class<? extends DataExtension> extClass = extension.getClass();

        for (Method method : extClass.getDeclaredMethods()) {
            Tab tab = method.getAnnotation(Tab.class);
            if (tab == null) {
                continue;
            }
            String tabName = tab.value();
            // Length restriction check
            if (tabName.length() > 50) {
                errors.add(extClass.getName() + "." + method.getName() + " tab name was over 50 characters.");
            }
            tabNames.add(tabName);
        }

        TabInfo.Multiple tabInfoAnnotations = extClass.getAnnotation(TabInfo.Multiple.class);
        if (tabInfoAnnotations == null) {
            // No tab info, go with default order
            return tabInformation;
        }

        for (TabInfo tabInfo : tabInfoAnnotations.value()) {
            String tabName = tabInfo.tab();

            // Length restriction check
            if (tabName.length() > 50) {
                errors.add(extClass.getName() + " tabName '" + tabName + "' was over 50 characters.");
            }

            if (!tabNames.contains(tabName)) {
                errors.add(extClass.getName() + " tab for '" + tabName + "' was not used.");
                continue;
            }

            tabInformation.add(tabInfo);
        }

        TabOrder tabOrder = extClass.getAnnotation(TabOrder.class);
        if (tabOrder != null) {
            for (String tabName : tabOrder.value()) {
                // Length restriction check
                if (tabName.length() > 50) {
                    errors.add(extClass.getName() + " tabName '" + tabName + "' found in TabOrder was over 50 characters.");
                }

                if (!tabNames.contains(tabName)) {
                    errors.add(extClass.getName() + " tab '" + tabName + "' found in TabOrder was not used.");
                }
            }

            Set<String> tabOrderNames = Arrays.stream(tabOrder.value()).collect(Collectors.toSet());
            for (String tabName : tabNames) {
                if (!tabOrderNames.contains(tabName)) {
                    errors.add(extClass.getName() + " tab '" + tabName + "' was not in TabOrder.");
                }
            }

        }

        return tabInformation;
    }

    public List<Conditional> extractConditionals(List<Method> booleanMethods) {
        Set<String> conditionNames = booleanMethods.stream()
                .map(method -> method.getAnnotation(BooleanProvider.class).conditionName())
                .collect(Collectors.toSet());

        List<Conditional> conditionals = new ArrayList<>();

        Class<? extends DataExtension> extClass = extension.getClass();
        for (Method method : extClass.getMethods()) {
            Conditional conditional = method.getAnnotation(Conditional.class);
            if (conditional == null) {
                continue;
            }

            conditionals.add(conditional);
        }

        for (Conditional conditional : conditionals) {
            String conditionName = conditional.value();
            if (conditionName.length() > 50) {
                errors.add(extClass.getName() + " '" + conditionName + "' conditionName was over 50 characters.");
            }

            if (!conditionNames.contains(conditionName)) {
                errors.add(extClass.getName() + " '" + conditionName + "' Condition was not provided by any BooleanProvider.");
            }
        }

        return conditionals;
    }

    public List<Method> extractBooleanMethods() {
        List<Method> booleanProviderMethods = new ArrayList<>();

        Class<? extends DataExtension> extClass = extension.getClass();
        for (Method method : extClass.getMethods()) {
            BooleanProvider provider = method.getAnnotation(BooleanProvider.class);
            if (provider == null) {
                continue;
            }

            // Return type check
            Class<?> returnType = method.getReturnType();
            if (!boolean.class.isAssignableFrom(returnType)) {
                errors.add(extClass.getName() + "." + method.getName() + " has invalid return type. was: " + returnType.getName() + ", expected: " + boolean.class.getName());
                continue;
            }

            // Cyclic conditional check
            Conditional conditional = method.getAnnotation(Conditional.class);
            if (conditional != null) {
                String conditionName = provider.conditionName();
                String requiredConditionName = conditional.value();

                if (!conditionName.isEmpty() && conditionName.equals(requiredConditionName)) {
                    errors.add(extClass.getName() + "." + method.getName() + " can not be conditional of itself. required condition: " + requiredConditionName + ", provided condition: " + conditionName);
                    continue;
                }
            }

            // Length restriction checks
            if (provider.text().length() > 50) {
                errors.add(extClass.getName() + "." + method.getName() + " text was over 50 characters.");
            }
            if (provider.description().length() > 150) {
                errors.add(extClass.getName() + "." + method.getName() + " description was over 150 characters.");
            }
            if (provider.conditionName().length() > 50) {
                errors.add(extClass.getName() + "." + method.getName() + " conditionName was over 50 characters.");
            }

            booleanProviderMethods.add(method);
        }

        return booleanProviderMethods;
    }

    public List<Method> extractNumberMethods() {
        List<Method> numberProviderMethods = new ArrayList<>();

        Class<? extends DataExtension> extClass = extension.getClass();
        for (Method method : extClass.getMethods()) {
            NumberProvider provider = method.getAnnotation(NumberProvider.class);
            if (provider == null) {
                continue;
            }

            // Return type check
            Class<?> returnType = method.getReturnType();
            if (!long.class.isAssignableFrom(returnType)) {
                errors.add(extClass.getName() + "." + method.getName() + " has invalid return type. was: " + returnType.getName() + ", expected: " + long.class.getName());
                continue;
            }

            // Length restriction checks
            if (provider.text().length() > 50) {
                errors.add(extClass.getName() + "." + method.getName() + " text was over 50 characters.");
            }
            if (provider.description().length() > 150) {
                errors.add(extClass.getName() + "." + method.getName() + " description was over 150 characters.");
            }

            numberProviderMethods.add(method);
        }

        return numberProviderMethods;
    }

    public List<Method> extractDoubleMethods() {
        List<Method> doubleProviderMethods = new ArrayList<>();

        Class<? extends DataExtension> extClass = extension.getClass();
        for (Method method : extClass.getMethods()) {
            DoubleProvider provider = method.getAnnotation(DoubleProvider.class);
            if (provider == null) {
                continue;
            }

            // Return type check
            Class<?> returnType = method.getReturnType();
            if (!double.class.isAssignableFrom(returnType)) {
                errors.add(extClass.getName() + "." + method.getName() + " has invalid return type. was: " + returnType.getName() + ", expected: " + double.class.getName());
                continue;
            }

            // Length restriction checks
            if (provider.text().length() > 50) {
                errors.add(extClass.getName() + "." + method.getName() + " text was over 50 characters.");
            }
            if (provider.description().length() > 150) {
                errors.add(extClass.getName() + "." + method.getName() + " description was over 150 characters.");
            }

            doubleProviderMethods.add(method);
        }

        return doubleProviderMethods;
    }

    public List<Method> extractPercentageMethods() {
        List<Method> percentageProviderMethods = new ArrayList<>();

        Class<? extends DataExtension> extClass = extension.getClass();
        for (Method method : extClass.getMethods()) {
            PercentageProvider provider = method.getAnnotation(PercentageProvider.class);
            if (provider == null) {
                continue;
            }

            // Return type check
            Class<?> returnType = method.getReturnType();
            if (!double.class.isAssignableFrom(returnType)) {
                errors.add(extClass.getName() + "." + method.getName() + " has invalid return type. was: " + returnType.getName() + ", expected: " + double.class.getName());
                continue;
            }

            // Length restriction checks
            if (provider.text().length() > 50) {
                errors.add(extClass.getName() + "." + method.getName() + " text was over 50 characters.");
            }
            if (provider.description().length() > 150) {
                errors.add(extClass.getName() + "." + method.getName() + " description was over 150 characters.");
            }

            percentageProviderMethods.add(method);
        }

        return percentageProviderMethods;
    }

    public List<Method> extractStringMethods() {
        List<Method> stringProviderMethods = new ArrayList<>();

        Class<? extends DataExtension> extClass = extension.getClass();
        for (Method method : extClass.getMethods()) {
            StringProvider provider = method.getAnnotation(StringProvider.class);
            if (provider == null) {
                continue;
            }

            // Return type check
            Class<?> returnType = method.getReturnType();
            if (!double.class.isAssignableFrom(returnType)) {
                errors.add(extClass.getName() + "." + method.getName() + " has invalid return type. was: " + returnType.getName() + ", expected: " + double.class.getName());
                continue;
            }

            // Length restriction checks
            if (provider.text().length() > 50) {
                errors.add(extClass.getName() + "." + method.getName() + " text was over 50 characters.");
            }
            if (provider.description().length() > 150) {
                errors.add(extClass.getName() + "." + method.getName() + " description was over 150 characters.");
            }

            stringProviderMethods.add(method);
        }

        return stringProviderMethods;
    }
}
