package main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.KillHandling;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * HandlingInfo Class for DeathEvent information when the dead entity is a
 * player.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class KillInfo extends HandlingInfo {

    private final LivingEntity dead;
    private final String weaponName;

    /**
     * Constructor.
     *
     * @param uuid       UUID of the killer.
     * @param time       Epoch ms the event occurred.
     * @param dead       Dead entity (Mob or Player)
     * @param weaponName Weapon used.
     */
    public KillInfo(UUID uuid, long time, LivingEntity dead, String weaponName) {
        super(uuid, InfoType.KILL, time);
        this.dead = dead;
        this.weaponName = weaponName;
    }

    @Override
    public void process(UserData uData) {
        if (!uData.getUuid().equals(uuid)) {
            return;
        }
        KillHandling.processKillInfo(uData, time, dead, weaponName);
    }
}
