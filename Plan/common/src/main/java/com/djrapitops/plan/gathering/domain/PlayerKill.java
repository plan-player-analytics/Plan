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
package com.djrapitops.plan.gathering.domain;

import com.djrapitops.plan.delivery.domain.DateHolder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a player vs player kill.
 *
 * @author Rsl1122
 */
public class PlayerKill implements DateHolder {

    private final UUID killer;
    private final UUID victim;
    private final String weapon;
    private final long date;

    private String victimName;
    private String killerName;

    /**
     * Creates a PlayerKill object with given parameters.
     *
     * @param killer UUID of the killer.
     * @param victim UUID of the victim.
     * @param weapon Weapon used.
     * @param date   Epoch millisecond at which the kill occurred.
     */
    public PlayerKill(UUID killer, UUID victim, String weapon, long date) {
        this.killer = killer;
        this.victim = victim;
        this.weapon = weapon;
        this.date = date;
    }

    public PlayerKill(UUID killer, UUID victim, String weapon, long date, String victimName) {
        this(killer, victim, weapon, date);
        this.victimName = victimName;
    }

    public PlayerKill(UUID killer, UUID victim, String weapon, long date, String victimName, String killerName) {
        this(killer, victim, weapon, date, victimName);
        this.killerName = killerName;
    }

    public UUID getKiller() {
        return killer;
    }

    public UUID getVictim() {
        return victim;
    }

    public Optional<String> getVictimName() {
        return Optional.ofNullable(victimName);
    }

    public Optional<String> getKillerName() {
        return Optional.ofNullable(killerName);
    }

    @Override
    public long getDate() {
        return date;
    }

    /**
     * Get the Weapon used as string.
     *
     * @return For example DIAMOND_SWORD
     */
    public String getWeapon() {
        return weapon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerKill that = (PlayerKill) o;
        return date == that.date &&
                Objects.equals(killer, that.killer) &&
                Objects.equals(victim, that.victim) &&
                Objects.equals(weapon, that.weapon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(killer, victim, date, weapon);
    }

    @Override
    public String toString() {
        return "PlayerKill{" +
                "killer=" + killer + ", " +
                "victim=" + victim + ", " +
                "date=" + date + ", " +
                "weapon='" + weapon + "'}";
    }

    public boolean isSelfKill() {
        return killer.equals(victim);
    }

    public boolean isNotSelfKill() {
        return !isSelfKill();
    }
}
