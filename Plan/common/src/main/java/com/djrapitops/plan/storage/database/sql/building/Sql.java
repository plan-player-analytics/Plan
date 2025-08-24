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

import com.djrapitops.plan.storage.database.DBType;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Duplicate String reducing utility class for SQL language Strings.
 */
public abstract class Sql {
    public static final String ID = "id";
    public static final String P_UUID = "uuid";

    public static final String INT = "integer";
    public static final String DOUBLE = "double";
    public static final String LONG = "bigint";
    public static final String BOOL = "boolean";

    public static final String SELECT = "SELECT ";
    public static final String DISTINCT = "DISTINCT ";
    public static final String FROM = " FROM ";
    public static final String DELETE_FROM = "DELETE" + FROM;
    public static final String WHERE = " WHERE ";
    public static final String GROUP_BY = " GROUP BY ";
    public static final String ORDER_BY = " ORDER BY ";
    public static final String INNER_JOIN = " JOIN ";
    public static final String LEFT_JOIN = " LEFT JOIN ";
    public static final String UNION = " UNION ";
    public static final String UNION_ALL = " UNION ALL ";
    public static final String AND = " AND ";
    public static final String OR = " OR ";
    public static final String IS_NULL = " IS NULL";
    public static final String IS_NOT_NULL = " IS NOT NULL";
    public static final String LIMIT = " LIMIT ";
    public static final String OFFSET = " OFFSET ";
    public static final String TEXT = "TEXT";

    private static final String FLOOR = "FLOOR(";
    private static final String MIN = "MIN(";
    private static final String MAX = "MAX(";
    private static final String VARCHAR = "varchar(";

    public static String nParameters(int n) {
        return new TextStringBuilder()
                .appendWithSeparators(IntStream.range(0, n).mapToObj(i -> "?").iterator(), ",")
                .toString();
    }

    public static String varchar(int length) {
        return VARCHAR + length + ')';
    }

    public static String floor(String expression) {return FLOOR + expression + ')';}

    public static String min(String expression) {return MIN + expression + ')';}

    public static String max(String expression) {return MAX + expression + ')';}

    /**
     * Turn day of week to epoch ms.
     * <p>
     * 1st of January 1970 (Epoch) is Thursday (-2).
     *
     * @param day 1 = Sunday, 2 = Monday etc.. 7 = Saturday
     * @return Milliseconds since epoch for this day to be given by {@link java.text.SimpleDateFormat} "EEEE"
     */
    public static long getDayEpochMs(int day) {
        return TimeUnit.DAYS.toMillis(day + 2L);
    }

    public static void setStringOrNull(PreparedStatement statement, int index, String value) throws SQLException {
        if (value != null) {
            statement.setString(index, value);
        } else {
            statement.setNull(index, Types.VARCHAR);
        }
    }

    public static void setDoubleOrNull(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value != null) {
            statement.setDouble(index, value);
        } else {
            statement.setNull(index, Types.DOUBLE);
        }
    }

    public static String concat(DBType dbType, String one, String two) {
        if (dbType == DBType.MYSQL) {
            return "CONCAT(" + one + ',' + two + ")";
        } else if (dbType == DBType.SQLITE) {
            return one + " || " + two;
        }
        return one + two;
    }

    public abstract String epochSecondToDate(String sql);

    public abstract String dateToEpochSecond(String sql);

    public abstract String dateToDayStamp(String sql);

    public abstract String dateToHourStamp(String sql);

    public abstract String dateToDayOfWeek(String sql);

    public abstract String dateToHour(String sql);

    public abstract String insertOrIgnore();

    // https://dev.mysql.com/doc/refman/5.7/en/date-and-time-functions.html
    public static class MySQL extends Sql {

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

        @Override
        public String dateToHourStamp(String sql) {
            return "DATE_FORMAT(" + sql + ",'%Y-%m-%d %H:00:00')";
        }

        @Override
        public String dateToDayOfWeek(String sql) {
            return "DAYOFWEEK(" + sql + ')';
        }

        @Override
        public String dateToHour(String sql) {
            return "HOUR(" + sql + ") % 24";
        }

        @Override
        public String insertOrIgnore() {
            return "INSERT IGNORE INTO ";
        }
    }

    // https://sqlite.org/lang_datefunc.html
    public static class SQLite extends Sql {

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

        @Override
        public String dateToHourStamp(String sql) {
            return "strftime('%Y-%m-%d %H:00:00'," + sql + ')';
        }

        @Override
        public String dateToDayOfWeek(String sql) {
            return "strftime('%w'," + sql + ")+1";
        }

        @Override
        public String dateToHour(String sql) {
            return "strftime('%H'," + sql + ')';
        }

        @Override
        public String insertOrIgnore() {
            return "INSERT OR IGNORE INTO ";
        }
    }
}
