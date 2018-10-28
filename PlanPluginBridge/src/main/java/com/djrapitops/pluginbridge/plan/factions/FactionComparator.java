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
package com.djrapitops.pluginbridge.plan.factions;

import com.massivecraft.factions.entity.Faction;

import java.util.Comparator;

/**
 * This class is used to compare factions in terms of Power.
 *
 * Compare method should only be used if FactionsHook.isEnabled() returns true.
 *
 * Note: this comparator imposes orderings that are inconsistent with equals.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see FactionsHook
 */
public class FactionComparator implements Comparator<Faction> {

    @Override
    public int compare(Faction fac1, Faction fac2) {
        return -Double.compare(fac1.getPower(), fac2.getPower());
    }
}
