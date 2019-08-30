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
package com.djrapitops.plan.system.storage.database.sql.parsing;

import static com.djrapitops.plan.system.storage.database.sql.parsing.Sql.*;

/**
 * @author Fuzzlemann
 */
public abstract class WhereParser extends SqlParser {

    private int conditions = 0;

    public WhereParser() {
        super();
    }

    public WhereParser(String start) {
        super(start);
    }

    public WhereParser where(String... conditions) {
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

    public WhereParser and(String condition) {
        append(AND);
        append('(').append(condition).append(')');
        this.conditions++;
        return this;
    }

    public WhereParser or(String condition) {
        append(OR);
        append('(').append(condition).append(')');
        this.conditions++;
        return this;
    }
}
