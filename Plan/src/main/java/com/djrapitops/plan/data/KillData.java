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

    public KillData(UUID victim, int victimID, String weapon, long date) {
        this.victim = victim;
        this.weapon = weapon;
        victimUserID = victimID;
        this.date = date;
    }

    public UUID getVictim() {
        return victim;
    }

    public long getDate() {
        return date;
    }

    public String getWeapon() {
        return weapon;
    }

    public int getVictimUserID() {
        return victimUserID;
    }
}
