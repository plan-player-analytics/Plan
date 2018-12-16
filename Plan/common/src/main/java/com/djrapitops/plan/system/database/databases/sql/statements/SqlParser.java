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
package com.djrapitops.plan.system.database.databases.sql.statements;

/**
 * Class for parsing different SQL strings.
 *
 * @author Rsl1122
 */
public class SqlParser {

    private final StringBuilder s;

    public SqlParser() {
        s = new StringBuilder();
    }

    public SqlParser(String start) {
        s = new StringBuilder(start);
    }

    public SqlParser addSpace() {
        s.append(" ");
        return this;
    }

    public SqlParser append(String string) {
        s.append(string);
        return this;
    }

    @Override
    public String toString() {
        return s.toString();
    }
}
