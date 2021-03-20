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
package com.djrapitops.plan.delivery.domain;

import org.junit.jupiter.api.RepeatedTest;
import utilities.RandomData;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AveragePingTest {

    @RepeatedTest(value = 5, name = "AveragePing equals {currentRepetition}/{totalRepetitions}")
    void twoAreEqual() {
        double value = RandomData.randomDouble();
        assertEquals(new AveragePing(value), new AveragePing(value));
    }

    @RepeatedTest(value = 5, name = "AveragePing not equals {currentRepetition}/{totalRepetitions}")
    void twoAreNotEqual() {
        double value = RandomData.randomDouble();
        assertNotEquals(new AveragePing(value), new AveragePing(value + 1.0));
    }


    @RepeatedTest(value = 5, name = "AveragePing hashing {currentRepetition}/{totalRepetitions}")
    void hashing() {
        double value = RandomData.randomDouble();

        Set<AveragePing> set = new HashSet<>();
        set.add(new AveragePing(value));
        set.add(new AveragePing(value));
        assertEquals(1, set.size());
    }

}