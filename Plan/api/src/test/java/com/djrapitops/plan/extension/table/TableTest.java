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
package com.djrapitops.plan.extension.table;

import com.djrapitops.plan.extension.icon.Icon;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TableTest {

    @Test
    void tableWithVaryingRowLengthsHasNoErrors() {
        Table.Factory table = Table.builder()
                .columnOne("", Icon.called("").build())
                .columnTwo("", Icon.called("").build())
                .columnThree("", Icon.called("").build())
                .columnFour("", Icon.called("").build())
                .columnFive("", Icon.called("").build());

        table.addRow();
        table.addRow("a");
        table.addRow("a", "b");
        table.addRow("a");
        table.addRow("a", "b", "c", "d", "e", "f");
        table.addRow("a", "b", "c", "d");

        List<Object[]> expected = Arrays.asList(
                new Object[]{"a", null, null, null, null},
                new Object[]{"a", "b", null, null, null},
                new Object[]{"a", null, null, null, null},
                new Object[]{"a", "b", "c", "d", "e"},
                new Object[]{"a", "b", "c", "d", null}
        );
        List<Object[]> result = table.build().getRows();

        for (int i = 0; i < expected.size(); i++) {
            assertArrayEquals(expected.get(i), result.get(i));
        }
        assertEquals(expected.size(), result.size());
    }

}