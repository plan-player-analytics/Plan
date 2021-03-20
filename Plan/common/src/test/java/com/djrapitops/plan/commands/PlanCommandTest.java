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
package com.djrapitops.plan.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlanCommandTest {

    private PlanCommand underTest;

    @BeforeEach
    void preparePlanCommand(@TempDir Path tempDir) throws Exception {
        PlanPluginComponent component = new PluginMockComponent(tempDir).getComponent();
        underTest = component.planCommand();
    }

    @Test
    void buildingHasNoBuilderErrors() {
        assertNotNull(underTest.build());
    }

}