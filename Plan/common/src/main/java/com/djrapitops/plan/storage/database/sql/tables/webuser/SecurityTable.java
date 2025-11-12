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
package com.djrapitops.plan.storage.database.sql.tables.webuser;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.objects.lookup.GroupIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about 'plan_security'
 *
 * @author AuroraLS3
 * @see com.djrapitops.plan.storage.database.transactions.patches.LinkedToSecurityTablePatch
 * @see com.djrapitops.plan.storage.database.transactions.patches.SecurityTableGroupPatch
 * @see com.djrapitops.plan.storage.database.transactions.patches.SecurityTableIdPatch
 */
public class SecurityTable {

    public static final String TABLE_NAME = "plan_security";

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String LINKED_TO = "linked_to_uuid";
    public static final String SALT_PASSWORD_HASH = "salted_pass_hash";
    public static final String GROUP_ID = "group_id";

    public static final String SELECT_ID_BY_USERNAME = SELECT + ID + FROM + TABLE_NAME + WHERE + USERNAME + "=?";
    public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " (" +
            USERNAME + ',' +
            LINKED_TO + ',' +
            SALT_PASSWORD_HASH + ',' +
            GROUP_ID + ") VALUES (?,?,?,(" + WebGroupTable.SELECT_GROUP_ID + "))";

    private SecurityTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, INT).primaryKey()
                .column(USERNAME, varchar(100)).notNull().unique()
                .column(LINKED_TO, varchar(36)).defaultValue("''")
                .column(SALT_PASSWORD_HASH, varchar(100)).notNull().unique()
                .column(GROUP_ID, INT).notNull()
                .foreignKey(GROUP_ID, WebGroupTable.TABLE_NAME, WebGroupTable.ID)
                .toString();
    }

    public static class Row implements GroupIdentifiable {
        public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " (" +
                USERNAME + ',' +
                LINKED_TO + ',' +
                SALT_PASSWORD_HASH + ',' +
                GROUP_ID + ") VALUES (?,?,?,?)";

        public int id;
        public String username;
        public String linkedToUuid;
        public String saltedPassHash;
        public int groupId;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.username = set.getString(USERNAME);
            row.linkedToUuid = set.getString(LINKED_TO);
            row.saltedPassHash = set.getString(SALT_PASSWORD_HASH);
            row.groupId = set.getInt(GROUP_ID);
            return row;
        }

        @Override
        public int getGroupId() {
            return groupId;
        }

        @Override
        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        public void insert(PreparedStatement statement) throws SQLException {
            statement.setString(1, username);
            statement.setString(2, linkedToUuid);
            statement.setString(3, saltedPassHash);
            statement.setInt(4, groupId);
        }
    }
}
