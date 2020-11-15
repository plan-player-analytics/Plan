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
import com.djrapitops.plan.placeholder.PlanPlaceholders;
import com.djrapitops.plan.utilities.logging.ErrorLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BukkitPlaceholderRegistrar {

    private final PlanPlaceholders placeholders;
    private final PlanSystem system;
    private final ErrorLogger errorLogger;

    @Inject
    public BukkitPlaceholderRegistrar(
            PlanPlaceholders placeholders,
            PlanSystem system,
            ErrorLogger errorLogger
    ) {
        this.placeholders = placeholders;
        this.system = system;
        this.errorLogger = errorLogger;
    }

    public void register() {
        new PlanPlaceholderExtension(placeholders, system, errorLogger).register();
    }
}
