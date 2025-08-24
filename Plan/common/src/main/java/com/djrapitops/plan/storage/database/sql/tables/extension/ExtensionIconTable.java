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

import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static com.djrapitops.plan.storage.database.sql.building.Sql.INT;

/**
 * Table information about 'plan_extension_icons'.
 *
 * @author AuroraLS3
 */
public class ExtensionIconTable {

    public static final String TABLE_NAME = "plan_extension_icons";

    public static final String ID = "id";
    public static final String ICON_NAME = "name";
    public static final String FAMILY = "family";
    public static final String COLOR = "color";

    public static void set3IconValuesToStatement(PreparedStatement statement, Icon icon) throws SQLException {
        set3IconValuesToStatement(statement, 1, icon);
    }

    public static void set3IconValuesToStatement(PreparedStatement statement, int parameterIndex, Icon icon) throws SQLException {
        if (icon != null) {
            statement.setString(parameterIndex, StringUtils.truncate(icon.getName(), 50));
            statement.setString(parameterIndex + 1, icon.getFamily().name());
            statement.setString(parameterIndex + 2, icon.getColor().name());
        } else {
            statement.setNull(parameterIndex, Types.VARCHAR);
            statement.setNull(parameterIndex + 1, Types.VARCHAR);
            statement.setNull(parameterIndex + 2, Types.VARCHAR);
        }
    }

    private ExtensionIconTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, INT).primaryKey()
                .column(ICON_NAME, Sql.varchar(50)).notNull().defaultValue("'question'")
                .column(FAMILY, Sql.varchar(15)).notNull().defaultValue("'" + Family.SOLID.name() + "'")
                .column(COLOR, Sql.varchar(25)).notNull().defaultValue("'" + Color.NONE.name() + "'")
                .build();
    }
}