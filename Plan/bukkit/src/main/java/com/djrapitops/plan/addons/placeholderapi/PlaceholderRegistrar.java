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
package com.djrapitops.plan.addons.placeholderapi;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plugin.logging.error.ErrorHandler;

/**
 * Additional wrapper to register PlaceholderAPI placeholders.
 *
 * @author Rsl1122
 */
public class PlaceholderRegistrar {

    private PlaceholderRegistrar() {
    }

    public static void register(PlanSystem system, ErrorHandler errorHandler) {
        new BukkitPlanPlaceHolders(system, errorHandler).register();
    }

}