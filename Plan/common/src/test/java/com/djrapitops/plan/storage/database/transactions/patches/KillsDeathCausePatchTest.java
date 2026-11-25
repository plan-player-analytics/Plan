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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.gathering.domain.DeathCause;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.KillsTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public interface KillsDeathCausePatchTest extends DatabaseTestPreparer {

    @Test
    default void patchIsNotAppliedWhenColumnAlreadyExists() {
        KillsDeathCausePatch patch = new KillsDeathCausePatch();
        db().executeTransaction(patch);
        assertFalse(patch.wasApplied());
    }

    @Test
    default void patchAddsColumnAndPopulatesUnambiguousExistingRows() {
        Database db = db();

        db.executeTransaction(new StoreServerPlayerTransaction(playerUUID, 0L, TestConstants.PLAYER_ONE_NAME, serverUUID(), "localhost"));
        db.executeTransaction(new StoreServerPlayerTransaction(player2UUID, 0L, TestConstants.PLAYER_TWO_NAME, serverUUID(), "localhost"));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[1]));

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        executeTransactions(new StoreSessionTransaction(session));

        List<String> legacyWeapons = List.of(
                "TNT",
                "Red Bed",
                "Respawn Anchor",
                "End Crystal",
                "Arrow",
                "WOLF",
                "Diamond Sword",
                "Trident"
        );

        // Recreate plan_kills without death_cause to simulate a pre-patch schema
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute("DROP TABLE " + KillsTable.TABLE_NAME);
                execute(CreateTableBuilder.create(KillsTable.TABLE_NAME, dbType)
                        .column(KillsTable.ID, Sql.INT).primaryKey()
                        .column(KillsTable.KILLER_UUID, Sql.varchar(36)).notNull()
                        .column(KillsTable.VICTIM_UUID, Sql.varchar(36)).notNull()
                        .column(KillsTable.SERVER_UUID, Sql.varchar(36)).notNull()
                        .column(KillsTable.WEAPON, Sql.varchar(KillsTable.WEAPON_COLUMN_LENGTH)).notNull()
                        .column(KillsTable.DATE, Sql.LONG).notNull()
                        .column(KillsTable.SESSION_ID, Sql.INT).notNull()
                        .foreignKey(KillsTable.SESSION_ID, SessionsTable.TABLE_NAME, SessionsTable.ID)
                        .toString());

                String insertOld = "INSERT INTO " + KillsTable.TABLE_NAME + " ("
                        + KillsTable.SESSION_ID + ','
                        + KillsTable.KILLER_UUID + ','
                        + KillsTable.VICTIM_UUID + ','
                        + KillsTable.SERVER_UUID + ','
                        + KillsTable.DATE + ','
                        + KillsTable.WEAPON
                        + ") VALUES ((SELECT " + SessionsTable.ID + " FROM " + SessionsTable.TABLE_NAME + " LIMIT 1), ?, ?, ?, ?, ?)";

                long time = 1000L;
                for (String weapon : legacyWeapons) {
                    long killTime = time++;
                    execute(new ExecStatement(insertOld) {
                        @Override
                        public void prepare(PreparedStatement statement) throws SQLException {
                            statement.setString(1, playerUUID.toString());
                            statement.setString(2, player2UUID.toString());
                            statement.setString(3, serverUUID().toString());
                            statement.setLong(4, killTime);
                            statement.setString(5, weapon);
                        }
                    });
                }
            }
        });

        KillsDeathCausePatch patch = new KillsDeathCausePatch();
        db.executeTransaction(patch);
        assertTrue(patch.wasApplied());

        // Second run must be a no-op
        KillsDeathCausePatch secondRun = new KillsDeathCausePatch();
        db.executeTransaction(secondRun);
        assertFalse(secondRun.wasApplied());

        Map<String, DeathCause> resultsByWeapon = db.query(KillQueries.fetchPlayerKillsOnServer(serverUUID(), 100))
                .stream()
                .collect(Collectors.toMap(PlayerKill::weapon, PlayerKill::deathCause));

        assertEquals(DeathCause.EXPLOSION, resultsByWeapon.get("TNT"));
        assertEquals(DeathCause.EXPLOSION, resultsByWeapon.get("Red Bed"));
        assertEquals(DeathCause.EXPLOSION, resultsByWeapon.get("Respawn Anchor"));
        assertEquals(DeathCause.EXPLOSION, resultsByWeapon.get("End Crystal"));
        assertEquals(DeathCause.PROJECTILE, resultsByWeapon.get("Arrow"));
        assertEquals(DeathCause.PET, resultsByWeapon.get("WOLF"));
        assertEquals(DeathCause.UNKNOWN, resultsByWeapon.get("Diamond Sword"));
        assertEquals(DeathCause.UNKNOWN, resultsByWeapon.get("Trident"));
    }
}