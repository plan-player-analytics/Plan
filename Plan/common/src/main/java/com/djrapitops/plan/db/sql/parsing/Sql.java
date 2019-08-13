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

    String epochSecondToDate(String sql);

    String dateToEpochSecond(String sql);

    String dateToDayStamp(String sql);

    // https://dev.mysql.com/doc/refman/5.7/en/date-and-time-functions.html
    class MySQL implements Sql {

        @Override
        public String epochSecondToDate(String sql) {
            return "FROM_UNIXTIME(" + sql + ')';
        }

        @Override
        public String dateToEpochSecond(String sql) {
            return "UNIX_TIMESTAMP(" + sql + ')';
        }

        @Override
        public String dateToDayStamp(String sql) {
            return "DATE(" + sql + ')';
        }

    }

    // https://h2database.com/html/functions.html
    class H2 extends MySQL {

        @Override
        public String epochSecondToDate(String sql) {
            return "DATEADD('SECOND', " + sql + ", DATE '1970-01-01')";
        }

        @Override
        public String dateToEpochSecond(String sql) {
            return "DATEDIFF('SECOND', DATE '1970-01-01', " + sql + ')';
        }

        @Override
        public String dateToDayStamp(String sql) {
            return "DATE(" + sql + ')';
        }

    }

    // https://sqlite.org/lang_datefunc.html
    class SQLite implements Sql {

        @Override
        public String epochSecondToDate(String sql) {
            return "datetime(" + sql + ", 'unixepoch')";
        }

        @Override
        public String dateToEpochSecond(String sql) {
            return "strftime('%s'," + sql + ")";
        }

        @Override
        public String dateToDayStamp(String sql) {
            return "strftime('%Y-%m-%d'," + sql + ')';
        }

    }
}
