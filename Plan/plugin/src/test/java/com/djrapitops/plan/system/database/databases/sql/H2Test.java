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
package com.djrapitops.plan.system.database.databases.sql;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Test for the H2 database
 *
 * @author Rsl1122, Fuzzlemann
 * @see SQLiteTest
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class H2Test extends CommonDBTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        handleSetup("H2");
    }

    @Test
    public void testH2GetConfigName() {
        assertEquals("h2", db.getType().getConfigName());
    }

    @Test
    public void testH2GetName() {
        assertEquals("H2", db.getType().getName());
    }
}
