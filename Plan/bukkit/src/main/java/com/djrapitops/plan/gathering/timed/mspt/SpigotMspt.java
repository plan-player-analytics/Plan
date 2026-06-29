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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Accessor for the average MSPT field in MinecraftServer class which is a long[] array with 100 values.
 *
 * @author AuroraLS3
 */
public class SpigotMspt {

    private static final AtomicBoolean PREPARED_SERVER = new AtomicBoolean(false);
    private static Reflection.FieldAccessor<long[]> msptField;
    private static Object server;

    private SpigotMspt() {
        /* Static method class */
    }

    public static Optional<long[]> getMspt() {
        return Optional.ofNullable(getValue());
    }

    private static long[] getValue() {
        try {
            prepField();
            prepServer();
            return msptField.get(server);
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError e) {
            return null;
        }
    }

    private static void prepServer() {
        if (!PREPARED_SERVER.get()) {
            server = Reflection.getMinecraftServer().orElse(null);
            PREPARED_SERVER.set(true);
        }
    }

    private static void prepField() {
        if (msptField == null) {
            Class<?> minecraftServerClass = Reflection.getMinecraftClass("MinecraftServer");
            msptField = Reflection.findField(minecraftServerClass, long[].class);
        }
    }

}
