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
package com.djrapitops.plan.storage.database.sql.tables.extension;

import com.djrapitops.plan.extension.extractor.ExtensionMethod.ParameterType;

/**
 * @author AuroraLS3
 */
public enum TableType {
    SERVER(0),
    PLAYER(1),
    GROUP(2);

    private final int type;

    TableType(int type) {
        this.type = type;
    }

    public static TableType fromType(int type) {
        for (TableType tableType : values()) {
            if (tableType.getType() == type) {
                return tableType;
            }
        }
        throw new IllegalArgumentException("Unknown table type: " + type);
    }

    public static TableType forParameterType(ParameterType parameterType) {
        return switch (parameterType) {
            case SERVER_NONE -> SERVER;
            case PLAYER_STRING, PLAYER_UUID -> PLAYER;
            case GROUP -> GROUP;
        };
    }

    public int getType() {
        return type;
    }
}
