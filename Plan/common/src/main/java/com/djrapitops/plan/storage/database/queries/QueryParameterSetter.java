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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.identification.ServerUUID;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.UUID;

public class QueryParameterSetter {

    private QueryParameterSetter() {}

    public static void setParameters(PreparedStatement statement, Object... parameters) throws SQLException {
        int index = 1;
        for (Object parameter : parameters) {
            if (parameter instanceof Object[]) {
                for (Object arrayParameter : (Object[]) parameter) {
                    setParameter(statement, index, arrayParameter);
                    index++;
                }
            } else if (parameter instanceof Collection) {
                for (Object collectionParameter : (Collection<?>) parameter) {
                    setParameter(statement, index, collectionParameter);
                    index++;
                }
            } else {
                setParameter(statement, index, parameter);
                index++;
            }
        }
    }

    private static void setParameter(PreparedStatement statement, int index, Object parameter) throws SQLException {
        if (parameter == null) {
            statement.setNull(index, Types.VARCHAR);
        } else if (parameter instanceof Boolean) {
            statement.setBoolean(index, (Boolean) parameter);
        } else if (parameter instanceof Integer) {
            statement.setInt(index, (Integer) parameter);
        } else if (parameter instanceof Long) {
            statement.setLong(index, (Long) parameter);
        } else if (parameter instanceof Double) {
            statement.setDouble(index, (Double) parameter);
        } else if (parameter instanceof Character) {
            statement.setString(index, String.valueOf(parameter));
        } else if (parameter instanceof Float) {
            statement.setFloat(index, (Float) parameter);
        } else if (parameter instanceof String) {
            statement.setString(index, (String) parameter);
        } else if (parameter instanceof UUID || parameter instanceof ServerUUID) {
            statement.setString(index, parameter.toString());
        }
    }

}
