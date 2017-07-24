package main.java.com.djrapitops.plan.data;

import java.util.Objects;
import java.util.UUID;

/**
 * This class is used to store data about a player kill inside the UserData
 * object.
 *
 * @author Rsl1122
 */
public class KillData {

    private final UUID victim;
    private final int victimUserID;
    private final long date;
    private final String weapon;

    /**
     * Creates a KillData object with given parameters.
     *
     * @param victim UUID of the victim.
     * @param victimID ID of the victim, get from the database.
     * @param weapon Weapon used.
     * @param date Epoch millisecond at which the kill occurred.
     */
    public KillData(UUID victim, int victimID, String weapon, long date) {
        this.victim = victim;
        this.weapon = weapon;
        victimUserID = victimID;
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

    /**
     * Get the Epoch millisecond the kill occurred.
     *
     * @return long in ms.
     */
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

    /**
     * Get the UserID of the victim, found from the database.
     *
     * @return For example: 6
     */
    public int getVictimUserID() {
        return victimUserID;
    }

    @Override
    public String toString() {
        return "{victim:" + victim + "|victimUserID:" + victimUserID + "|date:" + date + "|weapon:" + weapon + '}';
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
        return this.date == other.date
                && Objects.equals(this.weapon, other.weapon)
                && Objects.equals(this.victim, other.victim);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.victim);
        hash = 89 * hash + (int) (this.date ^ (this.date >>> 32));
        hash = 89 * hash + Objects.hashCode(this.weapon);
        return hash;
    }

}
