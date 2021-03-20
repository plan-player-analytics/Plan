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
package com.djrapitops.plan.extension;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ElementOrderTest {

    @Test
    void serializationCanBeDeserialized() {
        ElementOrder[] expected = ElementOrder.values();
        ElementOrder[] result = ElementOrder.deserialize(ElementOrder.serialize(expected));
        assertArrayEquals(expected, result);
    }

    @Test
    void singleElementSerializationCanBeDeserialized() {
        ElementOrder[] expected = new ElementOrder[]{ElementOrder.VALUES};
        ElementOrder[] result = ElementOrder.deserialize(ElementOrder.serialize(expected));
        assertArrayEquals(expected, result);
    }

    @Test
    void emptySerializationCanBeDeserialized() {
        ElementOrder[] result = ElementOrder.deserialize(ElementOrder.serialize(new ElementOrder[]{}));
        assertNull(result);
    }

    @Test
    void elementOrderValuesList() {
        List<ElementOrder> expected = Arrays.asList(ElementOrder.values());
        List<ElementOrder> result = ElementOrder.valuesAsList();
        assertEquals(expected, result);
    }

}