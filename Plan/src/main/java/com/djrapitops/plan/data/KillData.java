package main.java.com.djrapitops.plan.data;

import java.util.UUID;

/**
 *
 * @author Rsl1122
 */
public class KillData {

    private UUID victim;
    private int victimUserID;
    private long date;
    private String weapon;

    /**
     *
     * @param victim
     * @param victimID
     * @param weapon
     * @param date
     */
    public KillData(UUID victim, int victimID, String weapon, long date) {
        this.victim = victim;
        this.weapon = weapon;
        victimUserID = victimID;
        this.date = date;
    }

    /**
     *
     * @return
     */
    public UUID getVictim() {
        return victim;
    }

    /**
     *
     * @return
     */
    public long getDate() {
        return date;
    }

    /**
     *
     * @return
     */
    public String getWeapon() {
        return weapon;
    }

    /**
     *
     * @return
     */
    public int getVictimUserID() {
        return victimUserID;
    }
}
