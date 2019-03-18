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

/**
 * Table information about 'plan_extension_user_values'.
 *
 * @author Rsl1122
 */
public class ExtensionPlayerValues {

    public static final String TABLE_NAME = "plan_extension_user_values";

    public static final String ID = "id";
    public static final String PROVIDER_ID = "provider_id";
    public static final String USER_UUID = "uuid";

    public static final String BOOLEAN_VALUE = "boolean_value";
    public static final String DOUBLE_VALUE = "double_value";
    public static final String PERCENTAGE_VALUE = "percentage_value";
    public static final String LONG_VALUE = "long_value";
    public static final String STRING_VALUE = "string_value";
    public static final String GROUP_VALUE = "group_value";

}