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

/**
 * Class for building different SQL strings.
 *
 * @author AuroraLS3
 */
public class SqlBuilder {

    private final StringBuilder s;

    public SqlBuilder() {
        s = new StringBuilder();
    }

    public SqlBuilder(String start) {
        s = new StringBuilder(start);
    }

    public SqlBuilder append(String string) {
        s.append(string);
        return this;
    }

    public SqlBuilder append(char c) {
        s.append(c);
        return this;
    }

    @Override
    public String toString() {
        return s.toString();
    }
}
