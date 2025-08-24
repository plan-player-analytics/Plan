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
package com.djrapitops.plan.gathering.timed.mspt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author AuroraLS3
 */
public class PaperMsptField {

    private Method method;

    public boolean isAvailable() {
        try {
            //Only available in Paper
            method = Class.forName("org.bukkit.Bukkit").getMethod("getAverageTickTime");
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException noSuchMethodEx) {
            return false;
        }
    }

    public Optional<Double> getAverageTickTime() {
        if (method == null) {return Optional.empty();}
        try {
            return Optional.ofNullable((Double) method.invoke(null));
        } catch (IllegalAccessException | InvocationTargetException noSuchMethodEx) {
            return Optional.empty();
        }
    }

}
