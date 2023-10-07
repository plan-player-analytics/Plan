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
package com.djrapitops.plan.storage.database.transactions.events;

import com.djrapitops.plan.gathering.domain.PluginMetadata;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.PluginVersionTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Stores changes to the plugin list found during enable.
 *
 * @author AuroraLS3
 */
public class StorePluginVersionsTransaction extends Transaction {

    private final long time;
    private final ServerUUID serverUUID;
    private final List<PluginMetadata> changeList;

    public StorePluginVersionsTransaction(long time, ServerUUID serverUUID, List<PluginMetadata> changeList) {
        this.time = time;
        this.serverUUID = serverUUID;
        this.changeList = changeList;
    }

    @Override
    protected void performOperations() {
        execute(new ExecBatchStatement(PluginVersionTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (PluginMetadata plugin : changeList) {
                    statement.setString(1, serverUUID.toString());
                    statement.setString(2, StringUtils.truncate(plugin.getName(), PluginVersionTable.MAX_NAME_LENGTH));
                    if (plugin.getVersion() == null) {
                        statement.setNull(3, Types.VARCHAR);
                    } else {
                        statement.setString(3, StringUtils.truncate(plugin.getVersion(), PluginVersionTable.MAX_VERSION_LENGTH));
                    }
                    statement.setLong(4, time);
                    statement.addBatch();
                }
            }
        });
    }

    // Visible for testing
    public List<PluginMetadata> getChangeList() {
        return changeList;
    }
}
