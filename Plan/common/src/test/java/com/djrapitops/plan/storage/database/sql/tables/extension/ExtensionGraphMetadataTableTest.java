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
package com.djrapitops.plan.storage.database.sql.tables.extension;

import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.DOUBLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author AuroraLS3
 */
class ExtensionGraphMetadataTableTest {

    @ParameterizedTest(name = "Add columns from {0} to {1} expecting {2} new column alteration statements")
    @CsvSource({
            "1,1,0",
            "1,2,1",
            "1,5,4",
            "2,3,1",
            "2,4,2"
    })
    void alterTableGenerationSanityTest(int existingColumnCount, int newColumnCount, int expectedAddition) {
        List<String> result = ExtensionGraphMetadataTable.addColumnsStatements("test", "test", existingColumnCount, newColumnCount);
        assertEquals(expectedAddition, result.size(), () -> "Expanding from " + existingColumnCount + " to " + newColumnCount + " wanted " + expectedAddition + " statements but got " + result.size());
    }

    @ParameterizedTest(name = "Add column from {0} to {1} should add value_{2} column")
    @CsvSource({
            "1,2,2",
            "2,3,3",
            "3,4,4",
            "6,7,7",
    })
    void alterTableGenerationValueNSanityTest(int existingColumnCount, int newColumnCount, int expectedAddition) {
        List<String> result = ExtensionGraphMetadataTable.addColumnsStatements("test", "test", existingColumnCount, newColumnCount);
        List<String> expected = List.of("ALTER TABLE plan_extension_test_test ADD COLUMN value_" + expectedAddition + " " + DOUBLE);
        assertEquals(expected, result);
    }

}