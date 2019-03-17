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
package com.djrapitops.plan.db.sql.tables;

import com.djrapitops.plan.extension.icon.Icon;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Table information about 'plan_extension_icons'.
 *
 * @author Rsl1122
 */
public class ExtensionIconTable {

    public static final String TABLE_NAME = "plan_extension_icons";

    public static final String ID = "id";
    public static final String ICON_NAME = "name";
    public static final String FAMILY = "family";
    public static final String COLOR = "color";

    public static final String STATEMENT_SELECT_ICON_ID = "(" + SELECT + ID +
            FROM + TABLE_NAME +
            WHERE + ICON_NAME + "=?" +
            AND + FAMILY + "=?" +
            AND + COLOR + "=?)";

    public static void setIconValuesToStatement(PreparedStatement statement, Icon icon) throws SQLException {
        setIconValuesToStatement(statement, 1, icon);
    }

    public static void setIconValuesToStatement(PreparedStatement statement, int parameterIndex, Icon icon) throws SQLException {
        statement.setString(parameterIndex, icon.getName());
        statement.setString(parameterIndex + 1, icon.getFamily().name());
        statement.setString(parameterIndex + 2, icon.getColor().name());
    }
}