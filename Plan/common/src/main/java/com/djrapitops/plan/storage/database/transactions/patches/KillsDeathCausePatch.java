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
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.KillsTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Adds death_cause column to plan_kills and populates unambiguous existing rows.
 */
public class KillsDeathCausePatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(KillsTable.TABLE_NAME, KillsTable.DEATH_CAUSE);
    }

    @Override
    protected void applyPatch() {
        addColumn(
                KillsTable.TABLE_NAME,
                KillsTable.DEATH_CAUSE + " " + Sql.varchar(KillsTable.DEATH_CAUSE_COLUMN_LENGTH)
                        + " NOT NULL DEFAULT '" + DeathCause.UNKNOWN.name() + "'"
        );

        String lowerWeapon = "REPLACE(LOWER(" + KillsTable.WEAPON + "), '_', ' ')";
        // Legacy & cross-platform explosion weapon names:
        // - 'primed tnt': Bukkit 1.8-1.20.4 (PRIMED_TNT), Sponge 7, and Nukkit
        // - 'minecart tnt': Bukkit pre-1.20.5 (MINECART_TNT); 'minecart with tnt': Nukkit
        // - 'ender crystal': Bukkit pre-1.20.5 (ENDER_CRYSTAL)
        // - 'bed block': Bukkit pre-1.12 (BED_BLOCK, not matched by LIKE '%bed')
        // - 'firework': Bukkit pre-1.20.5 (FIREWORK) vs 'firework rocket' (1.20.5+, Fabric, Sponge)
        populateCause(DeathCause.EXPLOSION,
                lowerWeapon + " IN (" +
                        "'tnt', 'primed tnt', 'tnt minecart', 'minecart tnt', 'minecart with tnt', " +
                        "'end crystal', 'ender crystal', 'block explosion', 'respawn anchor', 'bed block', " +
                        "'firework', 'firework rocket')"
                        + OR + lowerWeapon + " LIKE '%bed'"
        );

        // 'thrown trident' is Nukkit-specific; plain 'trident' is intentionally left UNKNOWN
        // because it can be either melee or thrown on Bukkit/Fabric/Sponge.
        populateCause(DeathCause.PROJECTILE,
                lowerWeapon + " IN (" +
                        "'arrow', 'spectral arrow', 'tipped arrow', 'thrown trident', " +
                        "'fireball', 'small fireball', 'large fireball', 'dragon fireball', " +
                        "'snowball', 'egg', 'thrown egg', 'ender pearl', 'thrown ender pearl', " +
                        "'wither skull', 'shulker bullet', 'llama spit', " +
                        "'wind charge', 'breeze wind charge', " +
                        "'potion', 'splash potion', 'lingering potion', 'thrown potion', " +
                        "'thrown splash potion', 'thrown lingering potion')"
        );

        // - 'ocelot': tamed cats on Bukkit pre-1.14 and Nukkit
        // - 'craftwolf' / 'craftocelot': getClass().getSimpleName() fallback on Bukkit pre-1.9
        populateCause(DeathCause.PET,
                lowerWeapon + " IN ('wolf', 'cat', 'ocelot', 'parrot', 'craftwolf', 'craftocelot')"
        );
    }

    private void populateCause(DeathCause cause, String condition) {
        execute("UPDATE " + KillsTable.TABLE_NAME +
                " SET " + KillsTable.DEATH_CAUSE + "='" + cause.name() + "'" +
                WHERE + KillsTable.DEATH_CAUSE + "='" + DeathCause.UNKNOWN.name() + "'" +
                AND + "(" + condition + ")");
    }
}