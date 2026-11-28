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
package com.djrapitops.plan.storage.database.transactions.init;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.schema.MySQLSchemaQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionGroupsTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionFlagPlayerValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionFlagServerValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionFlagTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPlayerTableValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerTableValueTable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.apache.commons.text.TextStringBuilder;

/**
 * Transaction that creates the database index if it has not yet been created.
 *
 * @author AuroraLS3
 */
public class CreateIndexTransaction extends Transaction {

    @Override
    protected void performOperations() {
        createIndex(UsersTable.TABLE_NAME, "plan_users_uuid_index", UsersTable.USER_UUID);
        createIndex(ServerTable.TABLE_NAME, "plan_servers_uuid_index", ServerTable.SERVER_UUID);

        // replaced by foreign keys
        dropIndex(UserInfoTable.TABLE_NAME, "plan_user_info_uuid_index");
        // replaced by foreign keys
        dropIndex(SessionsTable.TABLE_NAME, "plan_sessions_uuid_index");

        createIndex(SessionsTable.TABLE_NAME, "plan_sessions_date_index", SessionsTable.SESSION_START);
        // Replaced by foreign keys
        dropIndex(WorldTimesTable.TABLE_NAME, "plan_world_times_uuid_index");

        createIndex(KillsTable.TABLE_NAME, "plan_kills_uuid_index",
                KillsTable.KILLER_UUID,
                KillsTable.VICTIM_UUID,
                KillsTable.SERVER_UUID
        );
        createIndex(KillsTable.TABLE_NAME, "plan_kills_date_index", KillsTable.DATE);
        // Replaced with foreign keys.
        dropIndex(PingTable.TABLE_NAME, "plan_ping_uuid_index");

        createIndex(PingTable.TABLE_NAME, "plan_ping_date_index", PingTable.DATE);
        createIndex(TPSTable.TABLE_NAME, "plan_tps_date_index", TPSTable.DATE);
        createIndex(TPSTable.TABLE_NAME, "plan_tps_server_date_index", TPSTable.SERVER_ID, TPSTable.DATE);

        createIndex(SessionsTable.TABLE_NAME, "plan_session_join_address_index", SessionsTable.JOIN_ADDRESS_ID);

        createIndex(ExtensionPlayerTableValueTable.TABLE_NAME, "plan_extension_player_table_value_player_index",
                ExtensionPlayerTableValueTable.TABLE_ID,
                ExtensionPlayerTableValueTable.USER_UUID);
        createIndex(ExtensionServerTableValueTable.TABLE_NAME, "plan_extension_server_table_value_server_index",
                ExtensionServerTableValueTable.TABLE_ID,
                ExtensionServerTableValueTable.SERVER_UUID);
        createIndex(ExtensionGroupsTable.TABLE_NAME, "plan_extension_groups_provider_user_index",
                ExtensionGroupsTable.PROVIDER_ID,
                ExtensionGroupsTable.USER_UUID);

        createIndex(UserInfoTable.TABLE_NAME, "plan_user_info_server_user",
                UserInfoTable.SERVER_ID,
                UserInfoTable.USER_ID);
        createIndex(SessionsTable.TABLE_NAME, "plan_sessions_server_time_user",
                SessionsTable.SERVER_ID,
                SessionsTable.SESSION_START,
                SessionsTable.USER_ID);
        createIndex(SessionsTable.TABLE_NAME, "plan_sessions_server_end_user",
                SessionsTable.SERVER_ID,
                SessionsTable.SESSION_END,
                SessionsTable.USER_ID);
        createIndex(SessionsTable.TABLE_NAME, "plan_sessions_user_end",
                SessionsTable.USER_ID, SessionsTable.SESSION_END);
        createIndex(SessionsTable.TABLE_NAME, "plan_session_user_start",
                SessionsTable.USER_ID, SessionsTable.SESSION_START);
        createIndex(WorldTimesTable.TABLE_NAME, "plan_world_times_user_server_world",
                WorldTimesTable.USER_ID, WorldTimesTable.SERVER_ID, WorldTimesTable.WORLD_ID);
        createIndex(StatisticValueTable.TABLE_NAME, "plan_statistic_value_user_server_statistic",
                StatisticValueTable.USER_ID, StatisticValueTable.SERVER_ID, StatisticValueTable.STATISTIC_ID);

        createUniqueIndex(ExtensionFlagTable.TABLE_NAME, "plan_extension_flag_provider_name",
                ExtensionFlagTable.PROVIDER_ID, ExtensionFlagTable.FLAG_NAME);
        createUniqueIndex(ExtensionFlagPlayerValueTable.TABLE_NAME, "plan_extension_player_flag_scope",
                ExtensionFlagPlayerValueTable.FLAG_ID, ExtensionFlagPlayerValueTable.USER_ID, ExtensionFlagPlayerValueTable.SERVER_ID);
        createIndex(ExtensionFlagPlayerValueTable.TABLE_NAME, "plan_extension_player_flag_lock",
                ExtensionFlagPlayerValueTable.USER_ID, ExtensionFlagPlayerValueTable.SERVER_ID, ExtensionFlagPlayerValueTable.FLAG_ID);
        createUniqueIndex(ExtensionFlagServerValueTable.TABLE_NAME, "plan_extension_server_flag_scope",
                ExtensionFlagServerValueTable.FLAG_ID, ExtensionFlagServerValueTable.SERVER_ID);
        createIndex(ExtensionFlagServerValueTable.TABLE_NAME, "plan_extension_server_flag_lock",
                ExtensionFlagServerValueTable.SERVER_ID, ExtensionFlagServerValueTable.FLAG_ID);
    }

    private void createIndex(String tableName, String indexName, String... indexedColumns) {
        createIndex(tableName, indexName, false, indexedColumns);
    }

    private void createUniqueIndex(String tableName, String indexName, String... indexedColumns) {
        createIndex(tableName, indexName, true, indexedColumns);
    }

    private void createIndex(String tableName, String indexName, boolean unique, String... indexedColumns) {
        if (indexedColumns.length == 0) {
            throw new IllegalArgumentException("Can not create index without columns");
        }

        boolean isMySQL = dbType == DBType.MYSQL;
        if (isMySQL) {
            boolean indexExists = query(MySQLSchemaQueries.doesIndexExist(indexName, tableName));
            if (indexExists) return;
        }

        TextStringBuilder sql = new TextStringBuilder("CREATE ");
        if (unique) {
            sql.append("UNIQUE ");
        }
        sql.append("INDEX ");
        if (!isMySQL) {
            sql.append("IF NOT EXISTS ");
        }
        sql.append(indexName).append(" ON ").append(tableName);

        sql.append(" (");
        sql.appendWithSeparators(indexedColumns, ",");
        sql.append(')');

        execute(sql.toString());
    }

    private void dropIndex(String tableName, String indexName) {
        boolean isMySQL = dbType == DBType.MYSQL;
        if (isMySQL) {
            boolean indexExists = query(MySQLSchemaQueries.doesIndexExist(indexName, tableName));
            if (!indexExists) return;
            execute("DROP INDEX " + indexName + " ON " + tableName);
        } else {
            execute("DROP INDEX IF EXISTS " + indexName);
        }
    }
}