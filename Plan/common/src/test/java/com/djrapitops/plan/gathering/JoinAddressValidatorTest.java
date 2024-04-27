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
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author AuroraLS3
 */
@ExtendWith(MockitoExtension.class)
class JoinAddressValidatorTest {

    @Mock
    PlanConfig config;
    @InjectMocks
    JoinAddressValidator joinAddressValidator;

    @DisplayName("Join address is valid")
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "play.domain.com",
            "12.34.56.78",
            "sub.play.domain.xz",
    })
    void validJoinAddresses(String address) {
        assertTrue(joinAddressValidator.isValid(address));
    }

    @DisplayName("Join address is invalid")
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "123",
            "kioels8bfbc80hgjpz25uatt5bi1n0ueffoqxvrl+q7wgbgynl9jm2w38pihx1nw", // https://github.com/plan-player-analytics/Plan/issues/3545
            "play.domain.com:25565",
            "play.domain.com:25565\u0000ouehfaounrfaeiurgea",
            "play.domain.com\u0000h59783g9guheorig",
            "PLAY.DOMAIN.COM:25565",
    })
    void invalidJoinAddresses(String address) {
        when(config.isTrue(DataGatheringSettings.PRESERVE_INVALID_JOIN_ADDRESS)).thenReturn(false);
        assertFalse(joinAddressValidator.isValid(address));
    }

    @Test
    @DisplayName("Empty join address is invalid")
    void invalidEmptyJoinAddresses() {
        assertFalse(joinAddressValidator.isValid(""));
    }

    @DisplayName("Join address sanitization works")
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "play.domain.com:25565, play.domain.com",
            "play.domain.com:25565\u0000ouehfaounrfaeiurgea, play.domain.com",
            "play.domain.com\u0000h59783g9guheorig, play.domain.com",
            "play.domain.comfmlJEI=1.32.5, play.domain.com",
            "PLAY.DOMAIN.COM:25565, PLAY.DOMAIN.COM", // Preserve case is on in the mocked config
    })
    void sanitizationTest(String address, String expected) {
        assertEquals(expected, joinAddressValidator.sanitize(address));
    }
}