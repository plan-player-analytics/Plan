/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling.info;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.LogoutHandling;
import org.bukkit.GameMode;

/**
 *
 * @author Rsl1122
 */
public class LogoutInfo extends HandlingInfo {

    private boolean banned;
    private SessionData sData;
    private GamemodeInfo gmInfo;

    /**
     *
     * @param uuid
     * @param time
     * @param banned
     * @param gm
     * @param sData
     */
    public LogoutInfo(UUID uuid, long time, boolean banned, GameMode gm, SessionData sData) {
        super(uuid, InfoType.LOGOUT, time);
        this.banned = banned;
        this.sData = sData;
        this.gmInfo = new GamemodeInfo(uuid, time, gm);
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
        uData.addSession(sData);
        LogoutHandling.processLogoutInfo(uData, time, banned);
        gmInfo.process(uData);
        return true;
    }

}
