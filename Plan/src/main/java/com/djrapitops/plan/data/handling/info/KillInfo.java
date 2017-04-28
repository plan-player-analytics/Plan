/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling.info;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.KillHandling;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Rsl1122
 */
public class KillInfo extends HandlingInfo {

    private LivingEntity dead;
    private String weaponName;

    /**
     *
     * @param uuid
     * @param time
     * @param dead
     * @param weaponName
     */
    public KillInfo(UUID uuid, long time, LivingEntity dead, String weaponName) {
        super(uuid, InfoType.KILL, time);
        this.dead = dead;
        this.weaponName = weaponName;
    }

    /**
     *
     * @param uData
     * @return
     */
    @Override
    public boolean process(UserData uData) {
        if (!uData.getUuid().equals(uuid)) {
            return false;
        }
        KillHandling.processKillInfo(uData, time, dead, weaponName);
        return true;
    }
}
