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

import com.djrapitops.plan.component.Component;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.Group;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.table.Table;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for different validations of ExtensionExtractor.
 * <p>
 * This Test class contains only INVALID implementations of the DataExtension API.
 *
 * @author AuroraLS3
 */
class ExtensionExtractorTest {

    @Test
    void pluginInfoIsRequired() {
        class Extension implements DataExtension {}

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Given class had no PluginInfo annotation", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void pluginInfoIsRequired2() {
        class Extension implements DataExtension {}

        assertEquals("Extension did not have @PluginInfo annotation!", assertThrows(IllegalArgumentException.class, new Extension()::getPluginName).getMessage());
    }

    @Test
    void providerMethodsAreRequired() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {}

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension class had no methods annotated with a Provider annotation", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void extensionNameIsAvailable() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {}

        assertEquals("Extension", new Extension().getPluginName());
    }

    @Test
    void publicProviderMethodsAreRequired() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @BooleanProvider(text = "Banned")
            private boolean method(UUID playerUUID) {
                return false;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension class had no methods annotated with a Provider annotation", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void pluginInfoNameOver50Chars() {
        @PluginInfo(name = "five five five five five five five five five five -")
        class Extension implements DataExtension {
            @BooleanProvider(text = "Required Provider")
            public boolean method() {
                return false;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Warnings: [Extension PluginInfo 'name' was over 50 characters.]", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void booleanProviderMustReturnBoolean() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @BooleanProvider(text = "Banned")
            public String method(UUID playerUUID) {
                return "false";
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid return type. was: java.lang.String, expected: boolean", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void booleanProviderMustReturnPrimitiveBoolean() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @BooleanProvider(text = "Banned")
            public Boolean method(UUID playerUUID) {
                return null;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid return type. was: java.lang.Boolean, expected: boolean", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void numberProviderMustReturnPrimitiveLong() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @NumberProvider(text = "Achievements")
            public Long method(UUID playerUUID) {
                return null;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid return type. was: java.lang.Long, expected: long", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void doubleProviderMustReturnPrimitiveDouble() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @DoubleProvider(text = "Money")
            public Double method(UUID playerUUID) {
                return null;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid return type. was: java.lang.Double, expected: double", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void percentageProviderMustReturnPrimitiveDouble() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @PercentageProvider(text = "Achievements awarded")
            public Double method(UUID playerUUID) {
                return null;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid return type. was: java.lang.Double, expected: double", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void stringProviderMustReturnString() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @StringProvider(text = "Town")
            public Double method(UUID playerUUID) {
                return null;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid return type. was: java.lang.Double, expected: java.lang.String", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void tableProviderMustReturnTable() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @TableProvider
            public Double method(UUID playerUUID) {
                return null;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid return type. was: java.lang.Double, expected: com.djrapitops.plan.extension.table.Table", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void groupProviderMustGroupArray() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @GroupProvider
            public Double method(UUID playerUUID) {
                return null;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid return type. was: java.lang.Double, expected: [Ljava.lang.String; (an array)", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void groupProviderMustGroupArray2() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @GroupProvider
            public Group method(UUID playerUUID) {
                return null;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid return type. was: com.djrapitops.plan.extension.Group, expected: [Ljava.lang.String; (an array)", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void dataBuilderProviderMustProvideDataBuilder() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @DataBuilderProvider
            public Group method(UUID playerUUID) {
                return null;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid return type. was: com.djrapitops.plan.extension.Group, expected: com.djrapitops.plan.extension.builder.ExtensionDataBuilder", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void booleanProviderCanNotSupplyItsOwnConditional() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            @BooleanProvider(text = "Banned", conditionName = "hasJoined")
            public boolean method(UUID playerUUID) {
                return false;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Warnings: [Extension.method can not be conditional of itself. required condition: hasJoined, provided condition: hasJoined]", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void conditionalMethodRequiresProvider() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            public boolean method(UUID playerUUID) {
                return false;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension class had no methods annotated with a Provider annotation", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void methodNeedsValidParameters() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            @BooleanProvider(text = "Banned", conditionName = "isBanned")
            public boolean method(Integer invalid) {
                return false;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has invalid parameter: 'java.lang.Integer' one of [class java.util.UUID, class java.lang.String, interface com.djrapitops.plan.extension.Group] is required as a parameter.", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void methodHasTooManyParameters() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            @BooleanProvider(text = "Banned", conditionName = "isBanned")
            public boolean method(String playerName, UUID playerUUID) {
                return false;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension.method has too many parameters, only one of [class java.util.UUID, class java.lang.String, interface com.djrapitops.plan.extension.Group] is required as a parameter.", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void methodsAreExtracted1() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            @BooleanProvider(text = "Test", conditionName = "isBanned")
            public boolean method() {
                return false;
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        Map<ExtensionMethod.ParameterType, ExtensionMethods> result = underTest.getMethods();
        Map<ExtensionMethod.ParameterType, ExtensionMethods> expected = buildExpectedExtensionMethodMap(extension, ExtensionMethods::addBooleanMethod);

        assertEquals(expected, result);
    }

    @Test
    void methodsAreExtracted2() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            @NumberProvider(text = "Test")
            public long method() {
                return 0;
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        Map<ExtensionMethod.ParameterType, ExtensionMethods> result = underTest.getMethods();
        Map<ExtensionMethod.ParameterType, ExtensionMethods> expected = buildExpectedExtensionMethodMap(extension, ExtensionMethods::addNumberMethod);

        assertEquals(expected, result);
    }

    @Test
    void methodsAreExtracted3() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            @DoubleProvider(text = "Test")
            public double method() {
                return 0;
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        Map<ExtensionMethod.ParameterType, ExtensionMethods> result = underTest.getMethods();
        Map<ExtensionMethod.ParameterType, ExtensionMethods> expected = buildExpectedExtensionMethodMap(extension, ExtensionMethods::addDoubleMethod);

        assertEquals(expected, result);
    }

    @Test
    void methodsAreExtracted4() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            @PercentageProvider(text = "Test")
            public double method() {
                return 0;
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        Map<ExtensionMethod.ParameterType, ExtensionMethods> result = underTest.getMethods();
        Map<ExtensionMethod.ParameterType, ExtensionMethods> expected = buildExpectedExtensionMethodMap(extension, ExtensionMethods::addPercentageMethod);

        assertEquals(expected, result);
    }

    @Test
    void methodsAreExtracted5() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            @StringProvider(text = "Test")
            public String method() {
                return "example";
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        Map<ExtensionMethod.ParameterType, ExtensionMethods> result = underTest.getMethods();
        Map<ExtensionMethod.ParameterType, ExtensionMethods> expected = buildExpectedExtensionMethodMap(extension, ExtensionMethods::addStringMethod);

        assertEquals(expected, result);
    }

    @Test
    void methodsAreExtracted6() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @TableProvider
            public Table method() {
                return Table.builder().build();
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        Map<ExtensionMethod.ParameterType, ExtensionMethods> result = underTest.getMethods();
        Map<ExtensionMethod.ParameterType, ExtensionMethods> expected = buildExpectedExtensionMethodMap(extension, ExtensionMethods::addTableMethod);

        assertEquals(expected, result);
    }

    @Test
    void methodsAreExtracted7() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @GroupProvider
            public String[] method(UUID playerUUID) {
                return new String[]{"example"};
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        Map<ExtensionMethod.ParameterType, ExtensionMethods> result = underTest.getMethods();

        Map<ExtensionMethod.ParameterType, ExtensionMethods> expected = new EnumMap<>(ExtensionMethod.ParameterType.class);
        for (ExtensionMethod.ParameterType value : ExtensionMethod.ParameterType.values()) {
            expected.put(value, new ExtensionMethods());
        }
        expected.get(ExtensionMethod.ParameterType.PLAYER_UUID).addGroupMethod(new ExtensionMethod(extension, extension.getClass().getMethod("method", UUID.class)));

        assertEquals(expected, result);
    }

    private Map<ExtensionMethod.ParameterType, ExtensionMethods> buildExpectedExtensionMethodMap(DataExtension extension, BiConsumer<ExtensionMethods, ExtensionMethod> addTo) throws NoSuchMethodException {
        Map<ExtensionMethod.ParameterType, ExtensionMethods> map = new EnumMap<>(ExtensionMethod.ParameterType.class);
        for (ExtensionMethod.ParameterType value : ExtensionMethod.ParameterType.values()) {
            map.put(value, new ExtensionMethods());
        }
        addTo.accept(
                map.get(ExtensionMethod.ParameterType.SERVER_NONE),
                new ExtensionMethod(extension, extension.getClass().getMethod("method"))
        );
        return map;
    }

    @Test
    void methodsAreExtracted8() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @DataBuilderProvider
            public ExtensionDataBuilder method() {
                return null;
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        Map<ExtensionMethod.ParameterType, ExtensionMethods> result = underTest.getMethods();
        Map<ExtensionMethod.ParameterType, ExtensionMethods> expected = buildExpectedExtensionMethodMap(extension, ExtensionMethods::addDataBuilderMethod);

        assertEquals(expected, result);
    }

    @Test
    void methodsAreExtracted9() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            @ComponentProvider(text = "Test")
            public Component method() {
                return null;
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        Map<ExtensionMethod.ParameterType, ExtensionMethods> result = underTest.getMethods();
        Map<ExtensionMethod.ParameterType, ExtensionMethods> expected = buildExpectedExtensionMethodMap(extension, ExtensionMethods::addComponentMethod);

        assertEquals(expected, result);
    }

    @Test
    void tabsAreExtracted() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Tab("Tab name")
            @DataBuilderProvider
            public ExtensionDataBuilder method() {
                return null;
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);

        Collection<Tab> expected = Collections.singletonList(extension.getClass().getMethod("method").getAnnotation(Tab.class));
        Collection<Tab> result = underTest.getTabAnnotations();
        assertEquals(expected, result);
    }

    @Test
    void conditionalsAreExtracted() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("example")
            @StringProvider(text = "Test")
            public String method() {
                return "example";
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);

        String expected = Stream.of(extension.getClass().getMethod("method").getAnnotation(Conditional.class))
                .map(Conditional::value).findFirst().orElseThrow(AssertionError::new);
        String result = underTest.getConditionalMethods().stream()
                .map(method -> new ExtensionMethod(extension, method))
                .map(extensionMethod -> extensionMethod.getAnnotation(Conditional.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Conditional::value)
                .findFirst().orElseThrow(AssertionError::new);
        assertEquals(expected, result);
    }

    @Test
    void textOver50CharsIsWarnedAbout() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @StringProvider(text = "aaaaaAAAAAbbbbbBBBBBcccccCCCCCdddddDDDDDeeeeeEEEEEfffffFFFF")
            public String method() {
                return "example";
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        assertEquals(
                "Warnings: [Extension.method 'text' was over 50 characters.]",
                assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage()
        );
    }

    @Test
    void descriptionOver50CharsIsWarnedAbout() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @StringProvider(
                    text = "a",
                    description = "aaaaaAAAAAbbbbbBBBBBcccccCCCCCdddddDDDDDeeeeeEEEEEaaaaaAAAAAbbbbbBBBBBcccccCCCCCdddddDDDDDeeeeeEEEEEaaaaaAAAAAbbbbbBBBBBcccccCCCCCdddddDDDDDeeeeeEEEEEfffffFFFFF"
            )
            public String method() {
                return "example";
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        assertEquals(
                "Warnings: [Extension.method 'description' was over 150 characters.]",
                assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage()
        );
    }

    @Test
    void dataBuilderProviderCanNotHaveConditional() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("bad")
            @DataBuilderProvider
            public ExtensionDataBuilder method() {
                return null;
            }
        }
        Extension extension = new Extension();
        ExtensionExtractor underTest = new ExtensionExtractor(extension);
        assertEquals(
                "Extension.method had Conditional, but DataBuilderProvider does not support it!",
                assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage()
        );
    }
}