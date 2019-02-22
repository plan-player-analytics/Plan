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
package com.djrapitops.pluginbridge.plan.towny;

import com.palmergames.bukkit.towny.object.Town;

import java.util.Comparator;

/**
 * This class is used to compare towns in terms of Amount of residents.
 *
 * Compare method should only be used if TownyHook.isEnabled() returns true.
 *
 * Note: this comparator imposes orderings that are inconsistent with equals.
 *
 * @author Rsl1122

 * @see TownyHook
 */
public class TownComparator implements Comparator<Town> {

    /**
     * Used to compare two Town objects.
     *
     * This method should only be used if TownyHook.isEnabled() returns true.
     *
     * Note: this comparator imposes orderings that are inconsistent with
     * equals.
     * @param tow1 Town 1
     * @param tow2 Town 2
     */
    @Override
    public int compare(Town tow1, Town tow2) {
        if (tow1.equals(tow2)) {
            return 0;
        }
        int tow1res = tow1.getNumResidents();
        int tow2res = tow2.getNumResidents();
        if (tow1res == tow2res) {
            return 0;
        } else if (tow1res > tow2res) {
            return 1;
        }
        return -1;
    }
}
