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
package com.djrapitops.plan.db.access.transactions.init;

import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.access.queries.objects.TPSQueries;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.access.transactions.commands.RemovePlayerTransaction;
import com.djrapitops.plan.db.sql.tables.PingTable;
import com.djrapitops.plan.db.sql.tables.SessionsTable;
import com.djrapitops.plan.db.sql.tables.TPSTable;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.RemoveUnsatisfiedConditionalResultsTransaction;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.console.PluginLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Transaction for cleaning up old data from the database.
 *
 * @author Rsl1122
 */
public class CleanTransaction extends Transaction {

    private final UUID serverUUID;

    private final long keepInactiveForMs;

    private final PluginLogger logger;
    private final Locale locale;

    public CleanTransaction(
            UUID serverUUID,
            long keepInactiveForMs,
            PluginLogger logger,
            Locale locale
    ) {
        this.serverUUID = serverUUID;
        this.keepInactiveForMs = keepInactiveForMs;
        this.logger = logger;
        this.locale = locale;
    }

    @Override
    protected void performOperations() {
        Optional<Integer> allTimePeak = query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID)).map(DateObj::getValue);

        execute(cleanTPSTable(allTimePeak.orElse(-1)));
        execute(cleanPingTable());

        // Clean DataExtension data
        executeOther(new RemoveUnsatisfiedConditionalResultsTransaction());

        int removed = cleanOldPlayers();
        if (removed > 0) {
            logger.info(locale.getString(PluginLang.DB_NOTIFY_CLEAN, removed));
        }
    }

    private int cleanOldPlayers() {
        long now = System.currentTimeMillis();
        long keepActiveAfter = now - keepInactiveForMs;

        List<UUID> inactivePlayers = query(fetchInactivePlayerUUIDs(keepActiveAfter));
        for (UUID uuid : inactivePlayers) {
            executeOther(new RemovePlayerTransaction(uuid));
        }
        return inactivePlayers.size();
    }

    private Executable cleanTPSTable(int allTimePlayerPeak) {
        String sql = "DELETE FROM " + TPSTable.TABLE_NAME +
                " WHERE (" + TPSTable.DATE + "<?)" +
                " AND (" + TPSTable.PLAYERS_ONLINE + "!=?)";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // More than 3 Months ago.
                long threeMonths = TimeAmount.MONTH.toMillis(3L);
                statement.setLong(1, System.currentTimeMillis() - threeMonths);
                statement.setInt(2, allTimePlayerPeak);
            }
        };
    }

    private Executable cleanPingTable() {
        String sql = "DELETE FROM " + PingTable.TABLE_NAME +
                " WHERE (" + PingTable.DATE + "<?)" +
                " OR (" + PingTable.MIN_PING + "<0)";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                long twoWeeks = TimeAmount.WEEK.toMillis(2L);
                statement.setLong(1, System.currentTimeMillis() - twoWeeks);
            }
        };
    }

    private Query<List<UUID>> fetchInactivePlayerUUIDs(long keepActiveAfter) {
        String sql = "SELECT uuid, last_seen FROM (SELECT" +
                " MAX(" + SessionsTable.SESSION_END + ") as last_seen, " + SessionsTable.USER_UUID +
                " FROM " + SessionsTable.TABLE_NAME +
                " GROUP BY " + SessionsTable.USER_UUID + ") as q1" +
                " WHERE last_seen < ?";
        return new QueryStatement<List<UUID>>(sql, 20000) {

            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, keepActiveAfter);
            }

            @Override
            public List<UUID> processResults(ResultSet set) throws SQLException {
                List<UUID> inactiveUUIDs = new ArrayList<>();
                while (set.next()) {
                    inactiveUUIDs.add(UUID.fromString(set.getString("uuid")));
                }
                return inactiveUUIDs;
            }
        };
    }
}