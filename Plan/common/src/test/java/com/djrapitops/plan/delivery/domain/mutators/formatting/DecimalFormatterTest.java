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
package com.djrapitops.plan.delivery.domain.mutators.formatting;

import com.djrapitops.plan.delivery.formatting.DecimalFormatter;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.FormatSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DecimalFormatter} class.
 *
 * @author AuroraLS3
 */
class DecimalFormatterTest {

    private DecimalFormatter underTest;

    @BeforeEach
    void setUpFormatter() {
        PlanConfig config = Mockito.mock(PlanConfig.class);
        when(config.get(FormatSettings.DECIMALS)).thenReturn("#.##");

        underTest = new DecimalFormatter(config);
    }

    @Test
    void cutDecimalsWhichIsRoundedDown() {
        double d = 0.05234;

        String result = underTest.apply(d);

        assertTrue("0.05".equals(result) || "0,05".equals(result));
    }

    @Test
    void cutDecimalsWhichIsRoundedUp() {
        double d = 0.05634;

        String result = underTest.apply(d);

        assertTrue("0.06".equals(result) || "0,06".equals(result));
    }

}