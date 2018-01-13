package com.djrapitops.plan.data.container;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.HasDate;
import com.djrapitops.plan.system.database.tables.Actions;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;
import java.util.UUID;

/**
 * This class is used to store data about a player kill inside the UserInfo
 * object.
 *
 * @author Rsl1122
 */
public class PlayerKill implements HasDate {

    private final UUID victim;
    private final long time;
    private final String weapon;

    /**
     * Creates a PlayerKill object with given parameters.
     *
     * @param victim UUID of the victim.
     * @param weapon Weapon used.
     * @param time   Epoch millisecond at which the kill occurred.
     */
    public PlayerKill(UUID victim, String weapon, long time) {
        this.victim = victim;
        this.weapon = weapon;
        this.time = time;
    }

    /**
     * Get the victim's UUID.
     *
     * @return UUID of the victim.
     */
    public UUID getVictim() {
        return victim;
    }

    /**
     * Get the Epoch millisecond the kill occurred.
     *
     * @return long in ms.
     */
    public long getTime() {
        return time;
    }

    @Override
    public long getDate() {
        return getTime();
    }

    /**
     * Get the Weapon used as string.
     *
     * @return For example DIAMOND_SWORD
     */
    public String getWeapon() {
        return weapon;
    }

    public Action convertToAction() {
        String name = Plan.getInstance().getDataCache().getName(victim);
        return new Action(time, Actions.KILLED, name + " with " + weapon);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerKill that = (PlayerKill) o;
        return time == that.time &&
                Objects.equals(victim, that.victim) &&
                Objects.equals(weapon, that.weapon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(victim, time, weapon);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("victim", victim)
                .append("time", time)
                .append("weapon", weapon)
                .toString();
    }
}
