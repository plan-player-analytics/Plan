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
package com.djrapitops.plan.delivery.formatting.time;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpLastModifiedDateFormatterTest {

    HttpLastModifiedDateFormatter underTest = new HttpLastModifiedDateFormatter(null, null);

    @ParameterizedTest
    @CsvSource(
            value = {
                    "0;Thu, 01 Jan 1970 00:00:00 GMT",
                    "1674306986180;Sat, 21 Jan 2023 13:16:26 GMT",
                    "214704830647000;Sat, 22 Sep 8773 14:44:07 GMT"
            }, delimiterString = ";"
    )
    void formatIsCorrect(long date, String expected) {
        String result = underTest.apply(date);
        assertEquals(expected, result);
    }
}