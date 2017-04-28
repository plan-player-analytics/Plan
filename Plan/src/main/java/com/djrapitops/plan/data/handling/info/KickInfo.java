/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling.info;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;

/**
 *
 * @author Rsl1122
 */
public class KickInfo extends HandlingInfo {

    /**
     *
     * @param uuid
     */
    public KickInfo(UUID uuid) {
        super(uuid, InfoType.KICK, 0L);
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
        uData.setTimesKicked(uData.getTimesKicked()+1);
        return true;
    }
    
}
