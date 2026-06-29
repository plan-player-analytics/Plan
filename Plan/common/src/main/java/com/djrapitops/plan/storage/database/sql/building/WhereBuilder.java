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
package com.djrapitops.plan.storage.database.sql.building;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * @author Fuzzlemann
 */
public abstract class WhereBuilder extends SqlBuilder {

    private int conditions = 0;

    protected WhereBuilder(String start) {
        super(start);
    }

    public WhereBuilder where(String... conditions) {
        append(WHERE);
        for (String condition : conditions) {
            if (this.conditions > 0) {
                append(AND);
            }
            append('(').append(condition).append(')');
            this.conditions++;
        }

        return this;
    }

    public WhereBuilder and(String condition) {
        append(AND);
        append('(').append(condition).append(')');
        this.conditions++;
        return this;
    }

    public WhereBuilder or(String condition) {
        append(OR);
        append('(').append(condition).append(')');
        this.conditions++;
        return this;
    }

    public WhereBuilder orderBy(String... columns) {
        int i = 0;
        append(ORDER_BY);
        for (String column : columns) {
            if (i > 0) {
                append(',');
            }
            append(column).append(" ASC");
            i++;
        }
        return this;
    }

    public WhereBuilder limit(int limit) {
        append(LIMIT);
        append(Integer.toString(limit));
        return this;
    }
}
