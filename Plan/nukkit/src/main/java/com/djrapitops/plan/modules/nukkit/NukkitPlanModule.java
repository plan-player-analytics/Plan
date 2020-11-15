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
package com.djrapitops.plan.modules.nukkit;

import com.djrapitops.plan.PlanNukkit;
import com.djrapitops.plan.PlanPlugin;
import dagger.Binds;
import dagger.Module;

/**
 * Dagger module for binding Plan instance.
 *
 * @author Rsl1122
 */
@Module
public interface NukkitPlanModule {

    @Binds
    PlanPlugin bindPlanPlugin(PlanNukkit plugin);

}