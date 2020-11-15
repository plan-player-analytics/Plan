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
import com.djrapitops.plan.extension.Group;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.table.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation detail, for extracting methods from {@link com.djrapitops.plan.extension.DataExtension}.
 * <p>
 * This class can be used for testing validity of annotation implementations
 * in your unit tests to avoid runtime errors. {@link ExtensionExtractor#validateAnnotations()}
 *
 * @author Rsl1122
 */
public final class ExtensionExtractor {

    private final DataExtension extension;
    private final String extensionName;

    private final List<String> warnings = new ArrayList<>();

    private PluginInfo pluginInfo;
    private TabOrder tabOrder;
    private List<TabInfo> tabInformation;
    private List<InvalidateMethod> invalidMethods;
    private MethodAnnotations methodAnnotations;

    private static final String WAS_OVER_50_CHARACTERS = "' was over 50 characters.";

    public ExtensionExtractor(DataExtension extension) {
        this.extension = extension;
        extensionName = extension.getClass().getSimpleName();
    }

    /**
     * Use this method in an unit test to validate your DataExtension.
     *
     * @throws IllegalArgumentException If an implementation error is found.
     */
    public void validateAnnotations() {
        extractAnnotationInformation();

        if (!warnings.isEmpty()) {
            throw new IllegalArgumentException("Warnings: " + warnings.toString());
        }
    }

    private static <V extends DataExtension, T extends Annotation> Optional<T> getClassAnnotation(Class<V> from, Class<T> ofClass) {
        return Optional.ofNullable(from.getAnnotation(ofClass));
    }

    public static <T extends DataExtension> String getPluginName(Class<T> extensionClass) {
        return getClassAnnotation(extensionClass, PluginInfo.class).map(PluginInfo::name)
                .orElseThrow(() -> new IllegalArgumentException("Given class had no PluginInfo annotation"));
    }

    private Method[] getMethods() {
        return extension.getClass().getMethods();
    }

    public void extractAnnotationInformation() {
        extractPluginInfo();
        extractInvalidMethods();

        extractMethodAnnotations();
        validateMethodAnnotations();

        validateConditionals();

        extractTabInfo();
    }

    private void extractMethodAnnotations() {
        methodAnnotations = new MethodAnnotations();

        for (Method method : getMethods()) {
            int modifiers = method.getModifiers();
            if (Modifier.isPrivate(modifiers)
                    || Modifier.isProtected(modifiers)
                    || Modifier.isStatic(modifiers)
                    || Modifier.isNative(modifiers)) {
                continue;
            }

            MethodAnnotations.get(method, BooleanProvider.class).ifPresent(annotation -> methodAnnotations.put(method, BooleanProvider.class, annotation));
            MethodAnnotations.get(method, NumberProvider.class).ifPresent(annotation -> methodAnnotations.put(method, NumberProvider.class, annotation));
            MethodAnnotations.get(method, DoubleProvider.class).ifPresent(annotation -> methodAnnotations.put(method, DoubleProvider.class, annotation));
            MethodAnnotations.get(method, PercentageProvider.class).ifPresent(annotation -> methodAnnotations.put(method, PercentageProvider.class, annotation));
            MethodAnnotations.get(method, StringProvider.class).ifPresent(annotation -> methodAnnotations.put(method, StringProvider.class, annotation));

            MethodAnnotations.get(method, Conditional.class).ifPresent(annotation -> methodAnnotations.put(method, Conditional.class, annotation));
            MethodAnnotations.get(method, Tab.class).ifPresent(annotation -> methodAnnotations.put(method, Tab.class, annotation));

            MethodAnnotations.get(method, TableProvider.class).ifPresent(annotation -> methodAnnotations.put(method, TableProvider.class, annotation));
            MethodAnnotations.get(method, GroupProvider.class).ifPresent(annotation -> methodAnnotations.put(method, GroupProvider.class, annotation));
        }

        if (methodAnnotations.isEmpty()) {
            throw new IllegalArgumentException(extensionName + " class had no methods annotated with a Provider annotation");
        }

        try {
            methodAnnotations.makeMethodsAccessible();
        } catch (SecurityException failedToMakeAccessible) {
            throw new IllegalArgumentException(extensionName + " has non accessible Provider method that could not be made accessible: " +
                    failedToMakeAccessible.getMessage(), failedToMakeAccessible);
        }
    }

    private <T> void validateReturnType(Method method, Class<T> expectedType) {
        Class<?> returnType = method.getReturnType();
        if (!expectedType.isAssignableFrom(returnType)) {
            String expectedName = expectedType.getName();
            throw new IllegalArgumentException(extensionName + "." + method.getName() +
                    " has invalid return type. was: " +
                    returnType.getName() +
                    ", expected: " +
                    (expectedName.startsWith("[L") ? expectedName + " (an array)" : expectedName));
        }
    }

    private void validateMethodAnnotationPropertyLength(String property, String name, int maxLength, Method method) {
        if (property.length() > maxLength) {
            warnings.add(extensionName + "." + method.getName() + " '" + name + WAS_OVER_50_CHARACTERS);
        }
    }

    private void validateMethodArguments(Method method, boolean parameterIsRequired, Class... parameterOptions) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        // Possible parameters for the methods:
        // UUID playerUUID, String playerName, Group group, none

        int parameters = parameterTypes.length;

        if (parameterIsRequired && parameters == 0) {
            // Does not have parameters, but one is required
            throw new IllegalArgumentException(extensionName + "." + method.getName() + " requires one of " + Arrays.toString(parameterOptions) + " as a parameter.");
        } else if (parameters == 0) {
            // Has no parameters & it is acceptable.
            return;
        }

        if (parameters > 1) {
            // Has too many parameters
            throw new IllegalArgumentException(extensionName + "." + method.getName() + " has too many parameters, only one of " + Arrays.toString(parameterOptions) + " is required as a parameter.");
        }

        Class<?> methodParameter = parameterTypes[0];

        boolean validParameter = false;
        for (Class<?> option : parameterOptions) {
            if (option.equals(methodParameter)) {
                validParameter = true;
                break;
            }
        }

        if (!validParameter) {
            // Has invalid parameter
            throw new IllegalArgumentException(extensionName + "." + method.getName() + " has invalid parameter: '" + methodParameter.getName() + "' one of " + Arrays.toString(parameterOptions) + " is required as a parameter.");
        }
        // Has valid parameter & it is acceptable.
    }

    private void validateMethodAnnotations() {
        validateBooleanProviderAnnotations();
        validateNumberProviderAnnotations();
        validateDoubleProviderAnnotations();
        validatePercentageProviderAnnotations();
        validateStringProviderAnnotations();
        validateTableProviderAnnotations();
        validateGroupProviderAnnotations();
    }

    private void validateBooleanProviderAnnotations() {
        for (Map.Entry<Method, BooleanProvider> booleanProvider : methodAnnotations.getMethodAnnotations(BooleanProvider.class).entrySet()) {
            Method method = booleanProvider.getKey();
            BooleanProvider annotation = booleanProvider.getValue();

            validateReturnType(method, boolean.class);
            validateMethodAnnotationPropertyLength(annotation.text(), "text", 50, method);
            validateMethodAnnotationPropertyLength(annotation.description(), "description", 150, method);
            validateMethodAnnotationPropertyLength(annotation.conditionName(), "conditionName", 50, method);
            validateMethodArguments(method, false, UUID.class, String.class, Group.class);

            String condition = MethodAnnotations.get(method, Conditional.class).map(Conditional::value).orElse(null);
            if (annotation.conditionName().equals(condition)) {
                warnings.add(extensionName + "." + method.getName() + " can not be conditional of itself. required condition: " + condition + ", provided condition: " + annotation.conditionName());
            }

            if (annotation.conditionName().isEmpty() && annotation.hidden()) {
                throw new IllegalArgumentException(extensionName + "." + method.getName() + " can not be 'hidden' without a 'conditionName'");
            }
        }
    }

    private void validateNumberProviderAnnotations() {
        for (Map.Entry<Method, NumberProvider> numberProvider : methodAnnotations.getMethodAnnotations(NumberProvider.class).entrySet()) {
            Method method = numberProvider.getKey();
            NumberProvider annotation = numberProvider.getValue();

            validateReturnType(method, long.class);
            validateMethodAnnotationPropertyLength(annotation.text(), "text", 50, method);
            validateMethodAnnotationPropertyLength(annotation.description(), "description", 150, method);
            validateMethodArguments(method, false, UUID.class, String.class, Group.class);
        }
    }

    private void validateDoubleProviderAnnotations() {
        for (Map.Entry<Method, DoubleProvider> doubleProvider : methodAnnotations.getMethodAnnotations(DoubleProvider.class).entrySet()) {
            Method method = doubleProvider.getKey();
            DoubleProvider annotation = doubleProvider.getValue();

            validateReturnType(method, double.class);
            validateMethodAnnotationPropertyLength(annotation.text(), "text", 50, method);
            validateMethodAnnotationPropertyLength(annotation.description(), "description", 150, method);
            validateMethodArguments(method, false, UUID.class, String.class, Group.class);
        }
    }

    private void validatePercentageProviderAnnotations() {
        for (Map.Entry<Method, PercentageProvider> percentageProvider : methodAnnotations.getMethodAnnotations(PercentageProvider.class).entrySet()) {
            Method method = percentageProvider.getKey();
            PercentageProvider annotation = percentageProvider.getValue();

            validateReturnType(method, double.class);
            validateMethodAnnotationPropertyLength(annotation.text(), "text", 50, method);
            validateMethodAnnotationPropertyLength(annotation.description(), "description", 150, method);
            validateMethodArguments(method, false, UUID.class, String.class, Group.class);
        }
    }

    private void validateStringProviderAnnotations() {
        for (Map.Entry<Method, StringProvider> stringProvider : methodAnnotations.getMethodAnnotations(StringProvider.class).entrySet()) {
            Method method = stringProvider.getKey();
            StringProvider annotation = stringProvider.getValue();

            validateReturnType(method, String.class);
            validateMethodAnnotationPropertyLength(annotation.text(), "text", 50, method);
            validateMethodAnnotationPropertyLength(annotation.description(), "description", 150, method);
            validateMethodArguments(method, false, UUID.class, String.class, Group.class);
        }
    }

    private void validateTableProviderAnnotations() {
        for (Method method : methodAnnotations.getMethodAnnotations(TableProvider.class).keySet()) {
            validateReturnType(method, Table.class);
            validateMethodArguments(method, false, UUID.class, String.class, Group.class);
        }
    }

    private void validateGroupProviderAnnotations() {
        for (Map.Entry<Method, GroupProvider> groupProvider : methodAnnotations.getMethodAnnotations(GroupProvider.class).entrySet()) {
            Method method = groupProvider.getKey();
            GroupProvider annotation = groupProvider.getValue();

            validateReturnType(method, String[].class);
            validateMethodAnnotationPropertyLength(annotation.text(), "text", 50, method);
            validateMethodArguments(method, true, UUID.class, String.class);
        }
    }

    private void validateConditionals() {
        Collection<Conditional> conditionals = methodAnnotations.getAnnotations(Conditional.class);
        Collection<BooleanProvider> conditionProviders = methodAnnotations.getAnnotations(BooleanProvider.class);

        Set<String> providedConditions = conditionProviders.stream().map(BooleanProvider::conditionName).collect(Collectors.toSet());

        for (Conditional condition : conditionals) {
            String conditionName = condition.value();

            if (conditionName.length() > 50) {
                warnings.add(extensionName + ": '" + conditionName + "' conditionName was over 50 characters.");
            }

            if (!providedConditions.contains(conditionName)) {
                warnings.add(extensionName + ": '" + conditionName + "' Condition was not provided by any BooleanProvider.");
            }
        }

        // Make sure that all methods annotated with Conditional have a Provider annotation
        Collection<Method> conditionalMethods = methodAnnotations.getMethodAnnotations(Conditional.class).keySet();
        for (Method conditionalMethod : conditionalMethods) {
            if (!MethodAnnotations.hasAnyOf(conditionalMethod,
                    BooleanProvider.class, DoubleProvider.class, NumberProvider.class,
                    PercentageProvider.class, StringProvider.class, TableProvider.class,
                    GroupProvider.class
            )) {
                throw new IllegalArgumentException(extensionName + "." + conditionalMethod.getName() + " did not have any associated Provider for Conditional.");
            }
        }
    }

    private <T extends Annotation> Optional<T> getClassAnnotation(Class<T> ofClass) {
        return getClassAnnotation(extension.getClass(), ofClass);
    }

    private void extractPluginInfo() {
        pluginInfo = getClassAnnotation(PluginInfo.class)
                .orElseThrow(() -> new IllegalArgumentException("Given class had no PluginInfo annotation"));

        if (pluginInfo.name().length() > 50) {
            warnings.add(extensionName + " PluginInfo 'name' was over 50 characters.");
        }
    }

    private void extractTabInfo() {
        tabInformation = new ArrayList<>();
        getClassAnnotation(TabInfo.Multiple.class).ifPresent(tabs -> {
            for (TabInfo tabInfo : tabs.value()) {
                String tabName = tabInfo.tab();

                // Length restriction check
                if (tabName.length() > 50) {
                    warnings.add(extensionName + " tabName '" + tabName + WAS_OVER_50_CHARACTERS);
                }

                tabInformation.add(tabInfo);
            }
        });

        tabOrder = getClassAnnotation(TabOrder.class).orElse(null);

        Map<Method, Tab> tabs = this.methodAnnotations.getMethodAnnotations(Tab.class);
        Set<String> tabNames = tabs.values().stream().map(Tab::value).collect(Collectors.toSet());

        // Check for unused TabInfo annotations
        for (TabInfo tabInfo : tabInformation) {
            String tabName = tabInfo.tab();

            if (tabName.length() > 50) {
                warnings.add(extensionName + " TabInfo " + tabName + " name was over 50 characters.");
            }

            if (!tabNames.contains(tabName)) {
                warnings.add(extensionName + " has TabInfo for " + tabName + ", but it is not used.");
            }
        }

        // Check Tab name lengths
        for (Map.Entry<Method, Tab> tab : tabs.entrySet()) {
            String tabName = tab.getValue().value();
            if (tabName.length() > 50) {
                warnings.add(extensionName + "." + tab.getKey().getName() + " Tab '" + tabName + "' name was over 50 characters.");
            }
        }
    }

    private void extractInvalidMethods() {
        invalidMethods = new ArrayList<>();
        getClassAnnotation(InvalidateMethod.Multiple.class).ifPresent(tabs -> {
            for (InvalidateMethod tabInfo : tabs.value()) {
                String methodName = tabInfo.value();

                // Length restriction check
                if (methodName.length() > 50) {
                    warnings.add(extensionName + " invalidated method '" + methodName + WAS_OVER_50_CHARACTERS);
                }

                invalidMethods.add(tabInfo);
            }
        });
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public Optional<TabOrder> getTabOrder() {
        return Optional.ofNullable(tabOrder);
    }

    public List<TabInfo> getTabInformation() {
        return tabInformation != null ? tabInformation : Collections.emptyList();
    }

    public MethodAnnotations getMethodAnnotations() {
        return methodAnnotations;
    }

    public List<InvalidateMethod> getInvalidateMethodAnnotations() {
        return invalidMethods != null ? invalidMethods : Collections.emptyList();
    }
}
