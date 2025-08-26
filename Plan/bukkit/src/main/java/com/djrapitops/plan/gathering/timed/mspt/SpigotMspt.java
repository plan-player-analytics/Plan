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

import com.djrapitops.plan.utilities.java.Reflection;

import java.util.Optional;

/**
 * Accessor for the average MSPT field in MinecraftServer class which is a long[] array with 100 values.
 *
 * @author AuroraLS3
 */
public class SpigotMspt {

    public static Optional<long[]> getMspt() {
        return Optional.ofNullable(getValue());
    }

    private static long[] getValue() {
        try {
            // Special thanks to Fuzzlemann for figuring out the methods required for this check.
            // https://github.com/plan-player-analytics/Plan/issues/769#issuecomment-433898242
            Class<?> minecraftServerClass = Reflection.getMinecraftClass("MinecraftServer");
            Object minecraftServer = Reflection.getField(minecraftServerClass, "SERVER", minecraftServerClass).get(null);

            return Reflection.findField(minecraftServerClass, long[].class).get(minecraftServer);
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError e) {
            return null;
        }
    }

}
