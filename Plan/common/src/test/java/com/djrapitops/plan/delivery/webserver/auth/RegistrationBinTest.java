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
package com.djrapitops.plan.delivery.webserver.auth;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.delivery.domain.auth.User;
import extension.FullSystemExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import utilities.TestConstants;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for registration bin which stores incomplete registrations in the database.
 *
 * @author AuroraLS3
 */
@ExtendWith(FullSystemExtension.class)
class RegistrationBinTest {
    @BeforeEach
    void setUp(PlanSystem system) {
        if (system != null) {system.enable();}
    }

    @AfterEach
    void tearDown(PlanSystem system) {
        if (system != null) {system.disable();}
    }

    @Test
    void userIsRegistered(RegistrationBin registrationBin) {
        String code = registrationBin.addInfoForRegistration("Test_Username", "Test_Password");
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .until(() -> registrationBin.contains(code));

        Optional<User> user = registrationBin.register(code, TestConstants.PLAYER_ONE_UUID);
        assertTrue(user.isPresent());
        assertTrue(user.get().doesPasswordMatch("Test_Password"));

        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .until(() -> !registrationBin.contains(code));
    }
}