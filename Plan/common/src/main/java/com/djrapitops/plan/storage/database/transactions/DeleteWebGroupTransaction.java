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
package com.djrapitops.plan.storage.database.transactions;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.sql.tables.SecurityTable;
import com.djrapitops.plan.storage.database.sql.tables.WebGroupTable;
import com.djrapitops.plan.storage.database.sql.tables.WebGroupToPermissionTable;
import org.intellij.lang.annotations.Language;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.DELETE_FROM;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Removes a web group from the database.
 *
 * @author AuroraLS3
 */
public class DeleteWebGroupTransaction extends Transaction {

    private final String name;
    private final String moveTo;

    public DeleteWebGroupTransaction(String name, String moveTo) {
        this.name = name;
        this.moveTo = moveTo;
    }

    @Override
    protected void performOperations() {
        String selectIdSql = WebGroupTable.SELECT_GROUP_ID;

        Integer moveToId = query(db -> db.queryOptional(selectIdSql, row -> row.getInt(WebGroupTable.ID), moveTo))
                .orElseThrow(() -> new DBOpException("Group not found for given name"));

        query(db -> db.queryOptional(selectIdSql, row -> row.getInt(WebGroupTable.ID), name))
                .ifPresent(groupId -> {
                            @Language("SQL")
                            String deletePermissionLinks = DELETE_FROM + WebGroupToPermissionTable.TABLE_NAME + WHERE + WebGroupToPermissionTable.GROUP_ID + "=?";
                            execute(new ExecStatement(deletePermissionLinks) {
                                @Override
                                public void prepare(PreparedStatement statement) throws SQLException {
                                    statement.setInt(1, groupId);
                                }
                            });

                            @Language("SQL")
                            String moveUsersSql = "UPDATE " + SecurityTable.TABLE_NAME + " SET " + SecurityTable.GROUP_ID + "=?" +
                                    WHERE + SecurityTable.GROUP_ID + "=?";
                            execute(new ExecStatement(moveUsersSql) {
                                @Override
                                public void prepare(PreparedStatement statement) throws SQLException {
                                    statement.setInt(1, groupId);
                                    statement.setInt(2, moveToId);
                                }
                            });

                            @Language("SQL")
                            String deleteGroup = DELETE_FROM + WebGroupTable.TABLE_NAME + WHERE + WebGroupTable.ID + "=?";
                            execute(new ExecStatement(deleteGroup) {
                                @Override
                                public void prepare(PreparedStatement statement) throws SQLException {
                                    statement.setInt(1, groupId);
                                }
                            });
                        }
                );
    }
}
