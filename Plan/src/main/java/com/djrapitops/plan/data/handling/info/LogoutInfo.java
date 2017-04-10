/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling.info;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.LogoutHandling;
import org.bukkit.GameMode;

/**
 *
 * @author Risto
 */
public class LogoutInfo extends HandlingInfo{
    private boolean banned;
    private GamemodeInfo gmInfo;

    public LogoutInfo(UUID uuid, long time, boolean banned, GameMode gm) {
        super(uuid, InfoType.LOGOUT, time);
        this.banned = banned;
        this.gmInfo = new GamemodeInfo(uuid, time, gm);
    }

    @Override
    public boolean process(UserData uData) {
        if (uData.getUuid() != uuid) {
            return false;
        }
        gmInfo.process(uData);
        LogoutHandling.processLogoutInfo(uData, time, banned);        
        return true;
    }

}
