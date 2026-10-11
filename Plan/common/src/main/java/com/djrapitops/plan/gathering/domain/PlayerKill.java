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
import com.djrapitops.plan.delivery.domain.PlayerIdentifier;
import com.djrapitops.plan.delivery.domain.ServerIdentifier;

import java.util.UUID;

/**
 * Represents a player vs player kill.
 *
 * @author AuroraLS3
 */
public record PlayerKill(Killer killer, Victim victim, String weapon, long date,
                         ServerIdentifier server) implements DateHolder {

    public PlayerKill(Killer killer, Victim victim, ServerIdentifier server, String weapon, long date) {
        this(killer, victim, weapon, date, server);
    }

    /**
     * Get the Weapon used as string.
     *
     * @return For example DIAMOND_SWORD
     */
    @Override
    public String weapon() {
        return weapon;
    }

    @Override
    public String toString() {
        return "PlayerKill{" +
                "killer=" + killer + ", " +
                "victim=" + victim + ", " +
                "server=" + server + ", " +
                "date=" + date + ", " +
                "weapon='" + weapon + "'}";
    }

    public boolean isSelfKill() {
        return killer.isSame(victim);
    }

    public boolean isNotSelfKill() {
        return !isSelfKill();
    }

    public String toJson() {
        return "{\"killer\": " + killer.toJson() + "," +
                "  \"victim\": " + victim.toJson() + "," +
                "  \"server\": " + server.toJson() + "," +
                "  \"weapon\": \"" + weapon + "\"," +
                "  \"date\": " + date +
                "}";
    }

    public static class Victim extends PlayerIdentifier {
        private final long registerDate;

        public Victim(UUID uuid, String name, long registerDate) {
            super(uuid, name);
            this.registerDate = registerDate;
        }

        public Victim(UUID uuid, String name) {
            this(uuid, name, 0L);
        }

        public long getRegisterDate() {
            return registerDate;
        }
    }

    public static class Killer extends PlayerIdentifier {
        public Killer(UUID uuid, String name) {
            super(uuid, name);
        }
    }
}
