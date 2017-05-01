/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling.info;

import java.net.InetAddress;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.LoginHandling;
import org.bukkit.GameMode;

/**
 *
 * @author Rsl1122
 */
public class ReloadInfo extends HandlingInfo {

    private InetAddress ip;
    private boolean banned;
    private String nickname;
    private GamemodeInfo gmInfo;

    /**
     *
     * @param uuid
     * @param time
     * @param ip
     * @param banned
     * @param nickname
     * @param gm
     */
    public ReloadInfo(UUID uuid, long time, InetAddress ip, boolean banned, String nickname, GameMode gm) {
        super(uuid, InfoType.RELOAD, time);
        this.ip = ip;
        this.banned = banned;
        this.nickname = nickname;
        gmInfo = new GamemodeInfo(uuid, time, gm);
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
        uData.setPlayTime(uData.getPlayTime() + (time - uData.getLastPlayed()));
        uData.setLastPlayed(time);        
        uData.updateBanned(banned);
        uData.addIpAddress(ip);
        uData.addNickname(nickname);
        LoginHandling.updateGeolocation(ip, uData);
        return gmInfo.process(uData);
    }

}
