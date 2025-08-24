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
package com.djrapitops.plan.storage.database.sql.building;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utilities.RandomData;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author AuroraLS3
 */
class SqlTest {

    @ParameterizedTest(name = "Generating {0} parameters generates {1}")
    @CsvSource(delimiter = ';', value = {
            "1;?",
            "2;?,?",
            "5;?,?,?,?,?"
    })
    void nParametersReturns(Integer n, String expected) {
        String result = Sql.nParameters(n);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Generating 0 parameters generates '' (empty string)")
    void zeroParametersReturnsEmpty() {
        String result = Sql.nParameters(0);
        assertEquals("", result);
    }

    @RepeatedTest(10)
    @DisplayName("Generating n (random) parameters generates n '?' characters and n-1 ',' characters")
    void randomParametersReturns() {
        int n = RandomData.randomInt(10, 50);

        String result = Sql.nParameters(n);
        int questions = 0;
        int commas = 0;
        for (char c : result.toCharArray()) {
            if (c == '?') {
                questions++;
            } else if (c == ',') {
                commas++;
            }
        }
        assertEquals(n, questions);
        assertEquals(n - 1, commas);
    }

}