package com.djrapitops.plan.data.container;


import com.djrapitops.plan.data.store.objects.DateHolder;

import java.util.Objects;
import java.util.UUID;

/**
 * This class is used to store data about a player kill inside the UserInfo
 * object.
 *
 * @author Rsl1122
 */
public class PlayerKill implements DateHolder {

    private final UUID victim;
    private final long date;
    private final String weapon;

    /**
     * Creates a PlayerKill object with given parameters.
     *
     * @param victim UUID of the victim.
     * @param weapon Weapon used.
     * @param date   Epoch millisecond at which the kill occurred.
     */
    public PlayerKill(UUID victim, String weapon, long date) {
        this.victim = victim;
        this.weapon = weapon;
        this.date = date;
    }

    /**
     * Get the victim's UUID.
     *
     * @return UUID of the victim.
     */
    public UUID getVictim() {
        return victim;
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
                Objects.equals(victim, that.victim) &&
                Objects.equals(weapon, that.weapon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(victim, date, weapon);
    }

    @Override
    public String toString() {
        return "PlayerKill{" +
                "victim=" + victim + ", " +
                "date=" + date + ", " +
                "weapon='" + weapon + "'}";
    }
}
