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
package com.djrapitops.plan.db.sql.parsing;

/**
 * Duplicate String reducing utility class for SQL language Strings.
 */
public interface Sql {
    String INT = "integer";
    String DOUBLE = "double";
    String LONG = "bigint";
    String BOOL = "boolean";

    String SELECT = "SELECT ";
    String DISTINCT = "DISTINCT ";
    String FROM = " FROM ";
    String DELETE_FROM = "DELETE" + FROM;
    String WHERE = " WHERE ";
    String GROUP_BY = " GROUP BY ";
    String ORDER_BY = " ORDER BY ";
    String INNER_JOIN = " JOIN ";
    String LEFT_JOIN = " LEFT JOIN ";
    String UNION = " UNION ";
    String AND = " AND ";
    String OR = " OR ";
    String IS_NULL = " IS NULL";
    String IS_NOT_NULL = " IS NOT NULL";

    static String varchar(int length) {
        return "varchar(" + length + ')';
    }

    String dateFromEpochSecond(String sql);

    String epochSecondFromDate(String sql);

    String dayOfYear(String sql);

    String year(String sql);

    class MySQL implements Sql {

        @Override
        public String dateFromEpochSecond(String sql) {
            return "FROM UNIXTIME(" + sql + ')';
        }

        @Override
        public String epochSecondFromDate(String sql) {
            return "UNIX TIMESTAMP(" + sql + ')';
        }

        @Override
        public String dayOfYear(String sql) {
            return "DAYOFYEAR(" + sql + ')';
        }

        @Override
        public String year(String sql) {
            return "YEAR(" + sql + ')';
        }
    }

    class H2 extends MySQL {

        @Override
        public String dateFromEpochSecond(String sql) {
            return "DATEADD('SECOND', " + sql + ", DATE '1970-01-01')";
        }

        @Override
        public String epochSecondFromDate(String sql) {
            return "DATEDIFF('SECOND', " + sql + ", DATE '1970-01-01')";
        }

        @Override
        public String dayOfYear(String sql) {
            return "DAY_OF_YEAR(" + sql + ')';
        }

        @Override
        public String year(String sql) {
            return "YEAR(" + sql + ')';
        }
    }

    class SQLite implements Sql {

        @Override
        public String dateFromEpochSecond(String sql) {
            return "datetime(" + sql + ", 'unixepoch')";
        }

        @Override
        public String epochSecondFromDate(String sql) {
            return "strftime('%s'," + sql + ")";
        }

        @Override
        public String dayOfYear(String sql) {
            return "strftime('%j'," + sql + ')';
        }

        @Override
        public String year(String sql) {
            return "strftime('%Y'" + sql + ')';
        }
    }
}
