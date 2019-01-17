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
package com.djrapitops.plan.data.store.mutators.formatting;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.FormatSettings;
import com.djrapitops.plan.utilities.formatting.DecimalFormatter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DecimalFormatter} class.
 *
 * @author Rsl1122
 */
public class DecimalFormatterTest {

    private DecimalFormatter underTest;

    @Before
    public void setUp() {
        PlanConfig config = Mockito.mock(PlanConfig.class);
        when(config.get(FormatSettings.DECIMALS)).thenReturn("#.##");

        underTest = new DecimalFormatter(config);
    }

    @Test
    public void testCutDecimalsWhichIsRoundedDown() {
        double d = 0.05234;

        String result = underTest.apply(d);

        assertTrue("0.05".equals(result) || "0,05".equals(result));
    }

    @Test
    public void testCutDecimalsWhichIsRoundedUp() {
        double d = 0.05634;

        String result = underTest.apply(d);

        assertTrue("0.06".equals(result) || "0,06".equals(result));
    }

}