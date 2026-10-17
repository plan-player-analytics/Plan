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
import com.djrapitops.plan.storage.database.queries.objects.lookup.TokenIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Represents the plan_api_token_to_permission table linking {@link ApiTokenTable} to {@link WebPermissionTable}.
 *
 * @author AuroraLS3
 */
public class ApiTokenToPermissionTable {

    public static final String TABLE_NAME = "plan_api_token_to_permission";

    public static final String ID = "id";
    public static final String TOKEN_ID = "token_id";
    public static final String PERMISSION_ID = "permission_id";

    public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " (" +
            TOKEN_ID + ',' + PERMISSION_ID + ") VALUES (?,?)";

    public static final String DELETE_BY_TOKEN_ID = DELETE_FROM + TABLE_NAME +
            WHERE + TOKEN_ID + "=?";

    public static final String SELECT_IDS = SELECT + TOKEN_ID + ',' + PERMISSION_ID +
            FROM + TABLE_NAME + ORDER_BY + TOKEN_ID + ',' + PERMISSION_ID;

    private ApiTokenToPermissionTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(TOKEN_ID, Sql.INT).notNull()
                .column(PERMISSION_ID, Sql.INT).notNull()
                .foreignKey(TOKEN_ID, ApiTokenTable.TABLE_NAME, ApiTokenTable.ID)
                .foreignKey(PERMISSION_ID, WebPermissionTable.TABLE_NAME, WebPermissionTable.ID)
                .toString();
    }

    public static class Row implements TokenIdentifiable {

        public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " (" +
                TOKEN_ID + ',' +
                PERMISSION_ID + ") VALUES (?, ?)";

        private int id;
        private int tokenId;
        private int permissionId;

        public Row() {
        }

        public Row(int id, int tokenId, int permissionId) {
            this.id = id;
            this.tokenId = tokenId;
            this.permissionId = permissionId;
        }

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.tokenId = set.getInt(TOKEN_ID);
            row.permissionId = set.getInt(PERMISSION_ID);
            return row;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public int getTokenId() {
            return tokenId;
        }

        @Override
        public void setTokenId(int tokenId) {
            this.tokenId = tokenId;
        }

        public int getPermissionId() {
            return permissionId;
        }

        public void setPermissionId(int permissionId) {
            this.permissionId = permissionId;
        }

        public void insert(PreparedStatement statement) throws SQLException {
            statement.setInt(1, tokenId);
            statement.setInt(2, permissionId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Row row = (Row) o;
            return id == row.id &&
                    tokenId == row.tokenId &&
                    permissionId == row.permissionId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, tokenId, permissionId);
        }

        @Override
        public String toString() {
            return "Row{" +
                    "id=" + id +
                    ", tokenId=" + tokenId +
                    ", permissionId=" + permissionId +
                    '}';
        }
    }
}
