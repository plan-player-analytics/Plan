package main.java.com.djrapitops.plan.data;

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
     * @param date Epoch millisecond at which the kill occurrred.
     */
    public KillData(UUID victim, int victimID, String weapon, long date) {
        this.victim = victim;
        this.weapon = weapon;
        victimUserID = victimID;
        this.date = date;
    }

    /**
     * Get the victim's UUID
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
}
