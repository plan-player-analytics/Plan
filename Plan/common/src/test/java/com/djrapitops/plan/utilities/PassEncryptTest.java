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
package com.djrapitops.plan.utilities;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utilities.RandomData;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Fuzzlemann
 */
class PassEncryptTest {

    private static final Map<String, String> PASSWORD_MAP = new HashMap<>();

    @BeforeAll
    static void setUpPasswords() {
        for (int i = 0; i < RandomData.randomInt(1, 10); i++) {
            String password = RandomData.randomString(RandomData.randomInt(5, 16));
            PASSWORD_MAP.put(password, PassEncryptUtil.createHash(password));
        }
    }

    @Test
    void testVerification() {
        for (Map.Entry<String, String> entry : PASSWORD_MAP.entrySet()) {
            String password = entry.getKey();
            String hash = entry.getValue();

            assertTrue(PassEncryptUtil.verifyPassword(password, hash));
            assertFalse(PassEncryptUtil.verifyPassword("WrongPassword", hash));
        }
    }
}
