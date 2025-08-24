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
package com.djrapitops.plan.extension.implementation.builder;

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExtensionDataBuilderTest {
    @Test
    void nullTextWhenCreatingValueBuilderThrowsException() {
        ExtDataBuilder builder = new ExtDataBuilder(new Extension());
        assertEquals(
                "'text' can't be null",
                assertThrows(IllegalArgumentException.class, () -> builder.valueBuilder(null)).getMessage()
        );
    }

    @Test
    void nullClassSupplierNotAdded() {
        ExtDataBuilder builder = new ExtDataBuilder(new Extension());
        builder.addValue(null, () -> null);
        assertEquals(Collections.emptyList(), builder.getValues());
    }

    @PluginInfo(name = "Extension")
    static class Extension implements DataExtension {}
}
