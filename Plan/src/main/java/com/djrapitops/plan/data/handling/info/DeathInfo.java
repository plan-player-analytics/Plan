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
 * @author Risto
 */
public class DeathInfo extends HandlingInfo{

    public DeathInfo(UUID uuid) {
        super(uuid, InfoType.DEATH, 0L);
    }
    
    @Override
    public boolean process(UserData uData) {
        if (uData.getUuid() != uuid) {
            return false;
        }
        uData.setDeaths(uData.getDeaths()+1);
        return true;
    }
}
