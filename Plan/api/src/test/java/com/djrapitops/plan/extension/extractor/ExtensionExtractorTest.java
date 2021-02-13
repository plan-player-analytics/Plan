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
import org.junit.jupiter.api.Test;

import java.util.UUID;

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
    void providerMethodsAreRequired() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {}

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Extension class had no methods annotated with a Provider annotation", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
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
        assertEquals("Extension.method did not have any associated Provider for Conditional.", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
    }

    @Test
    void conditionalNeedsToBeProvided() {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("hasJoined")
            @BooleanProvider(text = "Banned", conditionName = "isBanned")
            public boolean method(UUID playerUUID) {
                return false;
            }
        }

        ExtensionExtractor underTest = new ExtensionExtractor(new Extension());
        assertEquals("Warnings: [Extension: 'hasJoined' Condition was not provided by any BooleanProvider.]", assertThrows(IllegalArgumentException.class, underTest::validateAnnotations).getMessage());
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

}