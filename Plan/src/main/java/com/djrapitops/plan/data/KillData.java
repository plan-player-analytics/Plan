package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.database.tables.Actions;

import java.util.Objects;
import java.util.UUID;

/**
 * This class is used to store data about a player kill inside the UserInfo
 * object.
 *
 * @author Rsl1122
 */
public class KillData {

    private final UUID victim;
    private final long time;
    private final String weapon;

    /**
     * Creates a KillData object with given parameters.
     *
     * @param victim UUID of the victim.
     * @param weapon Weapon used.
     * @param time   Epoch millisecond at which the kill occurred.
     */
    public KillData(UUID victim, String weapon, long time) {
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

    /**
     * Get the Weapon used as string.
     *
     * @return For example DIAMOND_SWORD
     */
    public String getWeapon() {
        return weapon;
    }


    @Override
    public String toString() {
        return "{victim:" + victim + "|time:" + time + "|weapon:" + weapon + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KillData other = (KillData) obj;
        return this.time == other.time
                && Objects.equals(this.weapon, other.weapon)
                && Objects.equals(this.victim, other.victim);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.victim);
        hash = 89 * hash + (int) (this.time ^ (this.time >>> 32));
        hash = 89 * hash + Objects.hashCode(this.weapon);
        return hash;
    }

    public Action convertToAction() {
        return new Action(time, Actions.KILLED, "name with " + weapon); // TODO Name Cache.
    }
}
