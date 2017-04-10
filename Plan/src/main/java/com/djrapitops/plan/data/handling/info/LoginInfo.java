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
 * @author Risto
 */
public class LoginInfo extends HandlingInfo{
    private InetAddress ip;
    private boolean banned;
    private String nickname;
    private GamemodeInfo gmInfo;
    private int loginTimes;

    public LoginInfo(UUID uuid, long time, InetAddress ip, boolean banned, String nickname, GameMode gm, int loginTimes) {
        super(uuid, InfoType.LOGIN, time);
        this.ip = ip;
        this.banned = banned;
        this.nickname = nickname;
        this.gmInfo = new GamemodeInfo(uuid, time, gm);
        this.loginTimes = loginTimes;
    }
    
    public LoginInfo(UUID uuid, long time, InetAddress ip, boolean banned, String nickname, GameMode gm) {
        this(uuid, time, ip, banned, nickname, gm, 0);
    }

    @Override
    public boolean process(UserData uData) {
        if (uData.getUuid() != uuid) {
            return false;
        }
        LoginHandling.processLoginInfo(uData, getTime(), ip, banned, nickname, loginTimes);
        gmInfo.process(uData);
        return true;
    }
}
