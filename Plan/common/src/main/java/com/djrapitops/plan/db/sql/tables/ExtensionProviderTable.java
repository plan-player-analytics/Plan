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
 * Table information about 'plan_extension_providers'.
 *
 * @author Rsl1122
 */
public class ExtensionProviderTable {

    public static final String TABLE_NAME = "plan_extension_providers";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TEXT = "text"; // Can be null
    public static final String DESCRIPTION = "description"; // Can be null
    public static final String PRIORITY = "priority";
    public static final String GROUPABLE = "groupable"; // default false
    public static final String CONDITION = "condition"; // Can be null
    public static final String PLUGIN_ID = "plugin_id";
    public static final String ICON_ID = "icon_id";
    public static final String TAB_ID = "tab_id"; // Can be null

    public static final String PROVIDED_CONDITION = "provided_condition"; // Can be null
    public static final String FORMAT_TYPE = "format_type"; // Can be null
    public static final String PLAYER_NAME = "player_name"; // default false

}