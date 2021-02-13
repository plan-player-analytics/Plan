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
package com.djrapitops.plan;

import com.djrapitops.plan.exceptions.EnableException;

/**
 * Represents a system that can be enabled and disabled.
 *
 * @author AuroraLS3
 */
public interface SubSystem {

    /**
     * Performs enable actions for the subsystem.
     *
     * @throws EnableException If an error occurred during enable and it is fatal to the subsystem.
     */
    void enable();

    /**
     * Performs disable actions for the subsystem
     */
    void disable();

}
