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

import com.djrapitops.plan.delivery.domain.auth.ApiToken;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.building.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about plan_api_token table.
 *
 * @author AuroraLS3
 */
public class ApiTokenTable {

    public static final String TABLE_NAME = "plan_api_token";

    public static final String ID = "id";
    public static final String IDENTIFIER = "identifier";
    public static final String TOKEN = "token";
    public static final String ISSUED = "issued";
    public static final String LAST_USED = "last_used";

    public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " (" +
            IDENTIFIER + ", " +
            TOKEN + ',' +
            ISSUED + ',' +
            LAST_USED + ") VALUES (?,?,?,?)";

    public static final String UPDATE_LAST_USED_STATEMENT = UPDATE + TABLE_NAME +
            SET + LAST_USED + "=?" +
            WHERE + TOKEN + "=?";

    public static final String DELETE_BY_ID_STATEMENT = DELETE_FROM + TABLE_NAME +
            WHERE + ID + "=?";

    public static final String DELETE_BY_TOKEN_STATEMENT = DELETE_FROM + TABLE_NAME +
            WHERE + TOKEN + "=?";

    public static final String DELETE_ALL_STATEMENT = DELETE_FROM + TABLE_NAME;

    private ApiTokenTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(IDENTIFIER, Sql.varchar(500))
                .column(TOKEN, Sql.varchar(64)).notNull().unique()
                .column(ISSUED, Sql.LONG).notNull()
                .column(LAST_USED, Sql.LONG)
                .toString();
    }

    public static Query<List<Row>> fetchRows(int currentId, int rowLimit) {
        return db -> db.queryList(Select.all(TABLE_NAME)
                .where(ID + '>' + currentId)
                .orderBy(ID)
                .limit(rowLimit)
                .toString(), Row::extract);
    }

    public static class Row {
        private int id;
        private String identifier;
        private String token;
        private long issued;
        private Long lastUsed;

        public Row() {
        }

        public Row(int id, String identifier, String token, long issued, long lastUsed) {
            this.id = id;
            this.identifier = identifier;
            this.token = token;
            this.issued = issued;
            this.lastUsed = lastUsed;
        }

        public Row(ApiToken apiToken) {
            this(apiToken.id() != null ? apiToken.id() : 0, apiToken.identifier(), apiToken.token(), apiToken.issued(), apiToken.lastUsed());
        }

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.identifier = set.getString(IDENTIFIER);
            row.token = set.getString(TOKEN);
            row.issued = set.getLong(ISSUED);
            long lastUsed = set.getLong(LAST_USED);
            row.lastUsed = set.wasNull() ? null : lastUsed;
            return row;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public long getIssued() {
            return issued;
        }

        public void setIssued(long issued) {
            this.issued = issued;
        }

        public Long getLastUsed() {
            return lastUsed;
        }

        public void setLastUsed(Long lastUsed) {
            this.lastUsed = lastUsed;
        }

        public ApiToken toApiToken() {
            return new ApiToken(id, token, identifier, issued, lastUsed);
        }

        public void insert(PreparedStatement statement) throws SQLException {
            statement.setString(1, identifier);
            statement.setString(2, token);
            statement.setLong(3, issued);
            statement.setLong(4, lastUsed);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Row row = (Row) o;
            return id == row.id &&
                    issued == row.issued &&
                    Objects.equals(lastUsed, row.lastUsed) &&
                    Objects.equals(token, row.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, token, issued, lastUsed);
        }

        @Override
        public String toString() {
            return "Row{" +
                    "id=" + id +
                    ", identifier='" + identifier + '\'' +
                    ", token='" + token + '\'' +
                    ", issued=" + issued +
                    ", lastUsed=" + lastUsed +
                    '}';
        }
    }
}
