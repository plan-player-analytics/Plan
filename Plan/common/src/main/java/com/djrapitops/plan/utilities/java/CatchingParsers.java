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
package com.djrapitops.plan.utilities.java;

import com.djrapitops.plan.identification.ServerUUID;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Parsing utilities for parsing that return null on exception.
 *
 * @author AuroraLS3
 */
public class CatchingParsers {

    private CatchingParsers() {
        /* static method class */
    }

    public static @Nullable Long parseLong(@Nullable String string) {
        try {
            if (string == null) return null;
            return Long.parseLong(string);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static @Nullable ServerUUID parseServerUUID(String string) {
        try {
            if (string == null) return null;
            return ServerUUID.fromString(string);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static @Nullable UUID parsePlayerUUID(String string) {
        try {
            if (string == null) return null;
            return UUID.fromString(string);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
